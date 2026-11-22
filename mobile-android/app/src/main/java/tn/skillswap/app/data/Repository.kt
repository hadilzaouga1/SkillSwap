package tn.skillswap.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Request
import okhttp3.Response
import retrofit2.HttpException
import retrofit2.Retrofit
import tn.skillswap.app.BuildConfig
import tn.skillswap.app.data.local.AppDatabase
import tn.skillswap.app.data.local.CacheStore
import tn.skillswap.app.data.local.RoomCacheStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

private val Context.dataStore by preferencesDataStore("skillswap")

class Repository(private val context: Context) {

    private val kToken = stringPreferencesKey("token")
    private val kNom = stringPreferencesKey("nom")
    private val kRole = stringPreferencesKey("role")
    private val kEmail = stringPreferencesKey("email")
    private val kRefresh = stringPreferencesKey("refresh")
    private val kId = longPreferencesKey("id")

    @Volatile private var token: String? = null
    @Volatile private var refreshToken: String? = null
    private val verrou = Mutex()
    @Volatile var utilisateur: Utilisateur? = null
        private set

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; explicitNulls = false }

    private val http = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val b = chain.request().newBuilder()
            token?.let { b.header("Authorization", "Bearer $it") }
            chain.proceed(b.build())
        }
        // Jeton expiré (401) : on le renouvelle avec le refresh token puis on rejoue la requête une fois
        .authenticator { _, reponse -> reauthentifier(reponse) }
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    /** Client dédié à Keycloak : aucun log (les corps contiennent le mot de passe). */
    private val keycloak: KeycloakApi = Retrofit.Builder()
        .baseUrl(BuildConfig.KEYCLOAK_URL)
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(KeycloakApi::class.java)

    val api: Api = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(http)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(Api::class.java)

    private val database: AppDatabase by lazy { AppDatabase.creer(context) }

    /** Cache local Room (catalogue et échanges) pour le mode hors ligne. */
    val cache: CacheStore by lazy { RoomCacheStore(database.cacheDao()) }

    /** Adresse du serveur sans le suffixe /api/ (pour ouvrir les PDF de compétences). */
    fun urlAbsolue(chemin: String): String =
        if (chemin.startsWith("http")) chemin else BuildConfig.API_BASE_URL.removeSuffix("api/").trimEnd('/') + chemin

    suspend fun restaurerSession(): Boolean {
        val p = context.dataStore.data.first()
        val t = p[kToken] ?: return false
        token = t
        refreshToken = p[kRefresh]
        utilisateur = Utilisateur(p[kId] ?: 0, p[kNom] ?: "", p[kEmail] ?: "", p[kRole] ?: "ETUDIANT")
        return true
    }

    /**
     * Connexion via Keycloak (grant « password », client [BuildConfig.KEYCLOAK_CLIENT_ID]).
     * [identifiant] est l'email ou le nom d'utilisateur du compte Keycloak.
     * Le premier appel à /utilisateurs/me crée (ou rattache) l'utilisateur dans l'identity-service.
     */
    suspend fun connexion(identifiant: String, motDePasse: String) {
        val r = keycloak.token(BuildConfig.KEYCLOAK_REALM, BuildConfig.KEYCLOAK_CLIENT_ID, "password",
            username = identifiant.trim(), password = motDePasse)
        stocker(r)
        try {
            val profil = api.monProfil()
            utilisateur = Utilisateur(profil.id, profil.nom, profil.email, roleDepuisJwt(r.accessToken))
        } catch (e: Exception) {
            token = null; refreshToken = null
            throw e
        }
        val u = utilisateur!!
        context.dataStore.edit {
            it[kToken] = r.accessToken; r.refreshToken?.let { rt -> it[kRefresh] = rt }
            it[kNom] = u.nom; it[kEmail] = u.email; it[kRole] = u.role; it[kId] = u.id
        }
    }

    /** Page d'inscription Keycloak (le compte se crée dans le navigateur, puis on se connecte ici). */
    fun urlInscription(): String =
        BuildConfig.KEYCLOAK_URL + "realms/" + BuildConfig.KEYCLOAK_REALM + "/protocol/openid-connect/registrations" +
            "?client_id=" + BuildConfig.REGISTRATION_CLIENT_ID + "&response_type=code&scope=openid&redirect_uri=" +
            java.net.URLEncoder.encode(BuildConfig.REGISTRATION_REDIRECT, "UTF-8")

    private fun stocker(r: TokenResponse) {
        token = r.accessToken
        r.refreshToken?.let { refreshToken = it }
    }

    /** Renouvelle le jeton d'accès ; [ancien] évite un double renouvellement par des requêtes parallèles. */
    private suspend fun rafraichir(ancien: String?): String? = verrou.withLock {
        val courant = token
        if (courant != null && courant != ancien) return@withLock courant
        val rt = refreshToken ?: return@withLock null
        try {
            val r = keycloak.token(BuildConfig.KEYCLOAK_REALM, BuildConfig.KEYCLOAK_CLIENT_ID, "refresh_token", refreshToken = rt)
            stocker(r)
            context.dataStore.edit { it[kToken] = r.accessToken; r.refreshToken?.let { n -> it[kRefresh] = n } }
            r.accessToken
        } catch (_: Exception) {
            null
        }
    }

    private fun reauthentifier(reponse: Response): Request? {
        if (reponse.request.header("X-Deja-Rafraichi") != null) return null   // une seule tentative
        val ancien = reponse.request.header("Authorization")?.removePrefix("Bearer ")
        val nouveau = runBlocking { rafraichir(ancien) } ?: return null
        return reponse.request.newBuilder()
            .header("Authorization", "Bearer $nouveau")
            .header("X-Deja-Rafraichi", "1")
            .build()
    }

    suspend fun deconnexion() {
        token = null; refreshToken = null; utilisateur = null
        context.dataStore.edit { it.clear() }
        // Les données du cache appartiennent à l'ancien utilisateur : on les efface.
        withContext(Dispatchers.IO) { database.clearAllTables() }
    }

    suspend fun telechargerPdf(exchangeId: Long, support: Support): File {
        val dossier = File(context.cacheDir, "pdf").apply { mkdirs() }
        val fichier = File(dossier, "seance-$exchangeId-${support.id}.pdf")
        api.telechargerPdf(exchangeId, support.id).use { corps ->
            corps.byteStream().use { entree -> fichier.outputStream().use { entree.copyTo(it) } }
        }
        return fichier
    }

    suspend fun envoyerPdf(exchangeId: Long, octets: ByteArray, nomFichier: String, titre: String): Support {
        val fichier = MultipartBody.Part.createFormData(
            "file", nomFichier, octets.toRequestBody("application/pdf".toMediaType())
        )
        val titrePart = MultipartBody.Part.createFormData("title", titre)
        return api.ajouterPdf(exchangeId, fichier, titrePart)
    }
    suspend fun signalerFiche(exchangeId: Long) {
        try { api.syncNotificationFiche(exchangeId) } catch (_: Exception) {}
    }

    suspend fun signaler(exchangeId: Long) {
        try { api.syncNotification(exchangeId) } catch (_: Exception) {}
    }
}

/** Message lisible pour une réponse d'erreur HTTP (backend Spring ou Keycloak). */
fun messageDepuisReponse(code: Int, corps: String): String {
    val keycloak = Regex("\"error_description\"\\s*:\\s*\"([^\"]+)\"").find(corps)?.groupValues?.get(1)
    if (keycloak != null) return when {
        keycloak.contains("Invalid user credentials", ignoreCase = true) -> "Email ou mot de passe incorrect."
        keycloak.contains("not fully set up", ignoreCase = true) ->
            "Compte incomplet : renseignez prénom, nom et email dans Keycloak, puis réessayez."
        else -> keycloak
    }
    return Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(corps)?.groupValues?.get(1)
        ?: when (code) {
            401 -> "Identifiants invalides ou session expirée."
            403 -> "Action non autorisée."
            else -> "Erreur du serveur ($code)."
        }
}

fun Throwable.messageUtilisateur(): String = when (this) {
    is HttpException -> messageDepuisReponse(code(), response()?.errorBody()?.string().orEmpty())
    is IOException -> "Impossible de joindre le serveur. Vérifiez que le backend est démarré."
    else -> message ?: "Erreur inconnue."
}

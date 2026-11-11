package tn.skillswap.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import tn.skillswap.app.BuildConfig
import java.io.File
import java.io.IOException

private val Context.dataStore by preferencesDataStore("skillswap")

class Repository(private val context: Context) {

    private val kToken = stringPreferencesKey("token")
    private val kNom = stringPreferencesKey("nom")
    private val kRole = stringPreferencesKey("role")
    private val kId = longPreferencesKey("id")

    @Volatile private var token: String? = null
    @Volatile var utilisateur: Utilisateur? = null
        private set

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; explicitNulls = false }

    private val http = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val b = chain.request().newBuilder()
            token?.let { b.header("Authorization", "Bearer $it") }
            chain.proceed(b.build())
        }
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    val api: Api = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(http)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(Api::class.java)

    // ---------------- session ----------------
    suspend fun restaurerSession(): Boolean {
        val p = context.dataStore.data.first()
        val t = p[kToken] ?: return false
        token = t
        utilisateur = Utilisateur(p[kId] ?: 0, p[kNom] ?: "", "", p[kRole] ?: "ETUDIANT")
        return true
    }

    suspend fun connexion(email: String, motDePasse: String) {
        val r = api.connexion(ConnexionRequest(email.trim(), motDePasse))
        token = r.token
        utilisateur = r.utilisateur
        context.dataStore.edit {
            it[kToken] = r.token; it[kNom] = r.utilisateur.nom
            it[kRole] = r.utilisateur.role; it[kId] = r.utilisateur.id
        }
    }

    suspend fun deconnexion() {
        token = null; utilisateur = null
        context.dataStore.edit { it.clear() }
    }

    // ---------------- PDF ----------------

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
}

fun Throwable.messageUtilisateur(): String = when (this) {
    is HttpException -> {
        val corps = response()?.errorBody()?.string().orEmpty()
        Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(corps)?.groupValues?.get(1)
            ?: when (code()) {
                401 -> "Identifiants invalides ou session expirée."
                403 -> "Action non autorisée."
                else -> "Erreur du serveur (${code()})."
            }
    }
    is IOException -> "Impossible de joindre le serveur. Vérifiez que le backend est démarré."
    else -> message ?: "Erreur inconnue."
}

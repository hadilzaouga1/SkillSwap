package tn.skillswap.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long = 300,
)

/** Authentification auprès de Keycloak (realm skillswap). Les champs null ne sont pas envoyés. */
interface KeycloakApi {
    @FormUrlEncoded
    @POST("realms/{realm}/protocol/openid-connect/token")
    suspend fun token(
        @Path("realm") realm: String,
        @Field("client_id") clientId: String,
        @Field("grant_type") grantType: String,
        @Field("username") username: String? = null,
        @Field("password") password: String? = null,
        @Field("refresh_token") refreshToken: String? = null,
    ): TokenResponse
}

fun roleDepuisJwt(jwt: String): String = try {
    val charge = jwt.split('.')[1]
    val texte = String(java.util.Base64.getUrlDecoder().decode(charge), Charsets.UTF_8)
    val roles = Json.parseToJsonElement(texte).jsonObject["realm_access"]?.jsonObject?.get("roles")
        ?.jsonArray?.map { it.jsonPrimitive.content }.orEmpty()
    when {
        "ADMIN" in roles -> "ADMIN"
        "MODERATEUR" in roles -> "MODERATEUR"
        else -> "ETUDIANT"
    }
} catch (_: Exception) {
    "ETUDIANT"
}

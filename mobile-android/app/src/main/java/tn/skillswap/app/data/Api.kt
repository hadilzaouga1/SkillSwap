package tn.skillswap.app.data

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface Api {
    @POST("auth/connexion") suspend fun connexion(@Body body: ConnexionRequest): AuthResponse

    @GET("competences") suspend fun competences(@Query("motCle") motCle: String? = null): List<Competence>

    @POST("exchanges") suspend fun creerEchange(@Body body: ExchangeRequest): Exchange
    @GET("exchanges/my-requests") suspend fun envoyees(): List<Exchange>
    @GET("exchanges/received") suspend fun recues(): List<Exchange>
    @PATCH("exchanges/{id}/accept") suspend fun accepter(@Path("id") id: Long): Exchange
    @PATCH("exchanges/{id}/reject") suspend fun refuser(@Path("id") id: Long): Exchange
    @PATCH("exchanges/{id}/cancel") suspend fun annuler(@Path("id") id: Long): Exchange
    @PATCH("exchanges/{id}/complete") suspend fun terminer(@Path("id") id: Long): Exchange
    @PATCH("exchanges/{id}/confirm") suspend fun confirmer(@Path("id") id: Long): Exchange

    // --- séances ---
    @GET("sessions/by-exchange/{id}") suspend fun seance(@Path("id") id: Long): SeanceReponse
    @PUT("sessions/by-exchange/{id}") suspend fun enregistrerSeance(@Path("id") id: Long, @Body body: SeanceContenu): SeanceReponse
    @GET("sessions/summaries") suspend fun resumes(@Query("exchangeIds") ids: String): Map<String, ResumeSeance>
    @POST("sessions/by-exchange/{id}/resources/link") suspend fun ajouterLien(@Path("id") id: Long, @Body body: LienRequest): Support
    @Multipart
    @POST("sessions/by-exchange/{id}/resources/pdf")
    suspend fun ajouterPdf(
        @Path("id") id: Long,
        @Part file: MultipartBody.Part,
        @Part title: MultipartBody.Part,
    ): Support
    @Streaming @GET("sessions/by-exchange/{id}/resources/{rid}/file")
    suspend fun telechargerPdf(@Path("id") id: Long, @Path("rid") rid: Long): ResponseBody
    @DELETE("sessions/by-exchange/{id}/resources/{rid}")
    suspend fun supprimerSupport(@Path("id") id: Long, @Path("rid") rid: Long)
}

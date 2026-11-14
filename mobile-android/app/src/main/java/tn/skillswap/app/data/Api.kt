package tn.skillswap.app.data

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

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

    @GET("notifications") suspend fun notifications(): NotificationsResponse
    @PATCH("notifications/read-all") suspend fun toutLire(): UnreadCountResponse
    @PATCH("notifications/{id}/read") suspend fun lireNotification(@Path("id") id: Long): UnreadCountResponse
    @POST("notifications/exchange/{id}/sync") suspend fun syncNotification(@Path("id") id: Long): Map<String, Boolean>

    @POST("ai/chat") suspend fun iaChat(@Body body: IaChatRequest): IaChatResponse
    @POST("ai/improve-text") suspend fun iaAmeliorer(@Body body: IaImproveRequest): IaImproveResponse

    @GET("reviews/exchange/{id}") suspend fun avisEchange(@Path("id") id: Long): AvisEchange
    @POST("reviews/exchange/{id}") suspend fun publierAvis(@Path("id") id: Long, @Body body: AvisRequest): Avis
    @GET("reviews/user/{id}") suspend fun reputation(@Path("id") id: Long): Reputation
    @POST("auth/inscription") suspend fun inscription(@Body body: InscriptionRequest): AuthResponse
    @POST("ai/session-draft") suspend fun iaBrouillonSeance(@Body body: IaSeanceRequest): IaSeanceResponse
    @POST("notifications/exchange/{id}/session-updated") suspend fun syncNotificationFiche(@Path("id") id: Long): Map<String, Boolean>
    @GET("competences/{id}") suspend fun competence(@Path("id") id: Long): Competence
}
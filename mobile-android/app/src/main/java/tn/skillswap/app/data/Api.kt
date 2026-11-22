package tn.skillswap.app.data

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface Api {
    @GET("competences") suspend fun competences(
        @Query("motCle") motCle: String? = null,
        @Query("categorieId") categorieId: Long? = null,
        @Query("niveau") niveau: String? = null,
    ): List<Competence>
    @GET("categories") suspend fun categories(): List<Categorie>
    @GET("competences/mes-competences") suspend fun mesCompetences(): List<Competence>
    @POST("competences") suspend fun creerCompetence(@Body body: CompetenceRequest): Competence
    @PUT("competences/{id}") suspend fun modifierCompetence(@Path("id") id: Long, @Body body: CompetenceRequest): Competence
    @DELETE("competences/{id}") suspend fun supprimerCompetence(@Path("id") id: Long)

    @GET("credits/balance") suspend fun solde(): Solde
    @GET("credits/transactions") suspend fun transactions(): List<TransactionCredit>

    @GET("utilisateurs/me") suspend fun monProfil(): Profil
    @PUT("utilisateurs/me") suspend fun modifierProfil(@Body body: ProfilUpdate): Profil

    @POST("exchanges") suspend fun creerEchange(@Body body: ExchangeRequest): Exchange
    @GET("exchanges/my-requests") suspend fun envoyees(): List<Exchange>
    @GET("exchanges/received") suspend fun recues(): List<Exchange>
    @PATCH("exchanges/{id}/accept") suspend fun accepter(@Path("id") id: Long): Exchange
    @PATCH("exchanges/{id}/reject") suspend fun refuser(@Path("id") id: Long): Exchange
    @PATCH("exchanges/{id}/propose-time") suspend fun proposerCreneau(@Path("id") id: Long, @Body body: ProposeTimeRequest): Exchange
    @PATCH("exchanges/{id}/accept-proposal") suspend fun accepterCreneau(@Path("id") id: Long): Exchange
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
    @POST("ai/session-draft") suspend fun iaBrouillonSeance(@Body body: IaSeanceRequest): IaSeanceResponse
    @POST("notifications/exchange/{id}/session-updated") suspend fun syncNotificationFiche(@Path("id") id: Long): Map<String, Boolean>
    @GET("competences/{id}") suspend fun competence(@Path("id") id: Long): Competence
}
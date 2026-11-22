package tn.skillswap.app.data

import kotlinx.serialization.Serializable

@Serializable data class Utilisateur(val id: Long, val nom: String, val email: String, val role: String)

@Serializable
data class CategorieInfo(val id: Long = 0, val nom: String = "")

@Serializable
data class Competence(
    val id: Long,
    val titre: String,
    val description: String = "",
    val niveau: String = "",
    val disponibilites: String? = null,
    val statutValidation: String = "",
    val categorie: CategorieInfo? = null,
    val etudiantId: Long? = null,
    val documentUrl: String? = null,
    val documentName: String? = null,
    val documentSize: Long? = null,
)

@Serializable
data class Exchange(
    val id: Long,
    val requesterId: Long,
    val providerId: Long,
    val skillId: Long = 0,
    val skillTitle: String = "",
    val description: String? = null,
    val durationHours: Int = 1,
    val proposedDateTime: String = "",
    val status: String = "PENDING",
    val meetingLink: String? = null,
)

@Serializable
data class ExchangeRequest(
    val skillId: Long,
    val description: String,
    val durationHours: Int,
    val proposedDateTime: String,
)

@Serializable
data class Etape(val title: String, val description: String? = null, val minutes: Int? = null)

@Serializable
data class SeanceContenu(
    val title: String = "",
    val summary: String? = null,
    val description: String? = null,
    val objectives: List<String> = emptyList(),
    val outline: List<Etape> = emptyList(),
    val prerequisites: String? = null,
    val audience: String? = null,
    val level: String? = null,
    val language: String? = null,
    val format: String? = null,
    val mode: String? = null,
    val platform: String? = null,
    val meetingLink: String? = null,
    val location: String? = null,
    val deliveryNotes: String? = null,
    val materials: String? = null,
)

@Serializable
data class Support(
    val id: Long,
    val kind: String,
    val title: String,
    val description: String? = null,
    val url: String? = null,
    val host: String? = null,
    val fileName: String? = null,
    val size: Long? = null,
    val addedBy: Long = 0,
)

@Serializable data class Completude(val percent: Int = 0, val missing: List<String> = emptyList())

@Serializable
data class ExchangeResume(
    val id: Long,
    val skillId: Long? = null,
    val skillTitle: String? = null,
    val providerId: Long,
    val requesterId: Long,
    val durationHours: Double? = null,
    val proposedDateTime: String? = null,
    val status: String? = null
)

@Serializable
data class SeanceReponse(
    val exists: Boolean = false,
    val role: String = "REQUESTER",
    val canEdit: Boolean = false,
    val canAddResources: Boolean = false,
    val exchange: ExchangeResume = ExchangeResume(0, null, null, 0, 0, null, null, null),
    val completeness: Completude = Completude(),
    val session: SeanceContenu = SeanceContenu(),
    val resources: List<Support> = emptyList(),
)

@Serializable data class LienRequest(val title: String, val url: String)
@Serializable data class ResumeSeance(val exists: Boolean = false, val percent: Int = 0, val resources: Int = 0)

@Serializable
data class NotificationItem(
    val id: Long,
    val type: String,
    val title: String,
    val message: String,
    val link: String? = null,
    val exchangeId: Long? = null,
    val read: Boolean = false,
    val createdAt: String
)

@Serializable
data class NotificationsResponse(
    val unreadCount: Int = 0,
    val items: List<NotificationItem> = emptyList()
)

@Serializable
data class IaMsg(val role: String, val content: String)

@Serializable
data class IaContext(val page: String? = null)

@Serializable
data class IaChatRequest(
    val message: String,
    val history: List<IaMsg> = emptyList(),
    val context: IaContext = IaContext()
)

@Serializable
data class IaCompetence(
    val id: Long,
    val titre: String,
    val niveau: String? = null,
    val categorie: String? = null,
    val etudiantId: Long? = null
)

@Serializable
data class IaChatResponse(
    val reply: String,
    val suggestions: List<String> = emptyList(),
    val skills: List<IaCompetence> = emptyList(),
    val provider: String = "local"
)

@Serializable
data class IaImproveRequest(val text: String, val kind: String)

@Serializable
data class IaImproveResponse(val text: String, val tips: List<String> = emptyList())

@Serializable
data class IaSeanceRequest(
    val skillTitle: String,
    val level: String? = null,
    val durationHours: Double = 1.0,
    val mode: String? = null,
    val extra: String? = null
)

@Serializable
data class IaSeanceResponse(val draft: SeanceContenu, val provider: String = "local")

@Serializable
data class AvisEchange(
    val canReview: Boolean,
    val status: String,
    val mine: Avis? = null,
    val theirs: Avis? = null
)

@Serializable
data class Avis(
    val id: Long,
    val exchangeId: Long,
    val reviewerId: Long,
    val revieweeId: Long,
    val rating: Int,
    val comment: String? = null,
    val skillTitle: String? = null,
    val createdAt: String
)

@Serializable
data class AvisRequest(val rating: Int, val comment: String? = null)

@Serializable
data class Reputation(
    val userId: Long,
    val average: Double,
    val count: Int,
    val distribution: Map<String, Int>,
    val badge: Badge? = null,
    val reviews: List<Avis> = emptyList()
)

@Serializable
data class Badge(val code: String, val label: String)

@Serializable data class UnreadCountResponse(val unreadCount: Int = 0)
@Serializable
data class Solde(val userId: Long = 0, val balance: Int = 0, val reserved: Int = 0, val available: Int = 0)

@Serializable
data class TransactionCredit(
    val id: Long,
    val type: String,
    val amount: Int,
    val balanceAfter: Int = 0,
    val exchangeId: Long? = null,
    val description: String? = null,
    val createdAt: String = "",
)

// ---------------------------------------------------------------- Compétences (skill-service)
@Serializable
data class Categorie(val id: Long, val nom: String)

@Serializable
data class CompetenceRequest(
    val titre: String,
    val description: String,
    val niveau: String,
    val categorieId: Long,
    val disponibilites: String? = null,
)
@Serializable
data class Profil(
    val id: Long,
    val nom: String,
    val email: String = "",
    val universite: String? = null,
    val filiere: String? = null,
    val noteMoyenne: Double? = null,
)
@Serializable
data class ProfilUpdate(val nom: String, val universite: String? = null, val filiere: String? = null)
@Serializable
data class ProposeTimeRequest(val proposedDateTime: String)

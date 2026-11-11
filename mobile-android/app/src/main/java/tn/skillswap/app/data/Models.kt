package tn.skillswap.app.data

import kotlinx.serialization.Serializable

@Serializable data class ConnexionRequest(val email: String, val motDePasse: String)

@Serializable data class Utilisateur(val id: Long, val nom: String, val email: String, val role: String)

@Serializable data class AuthResponse(val token: String, val utilisateur: Utilisateur)

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
)

@Serializable
data class ExchangeRequest(
    val skillId: Long,
    val description: String,
    val durationHours: Int,
    val proposedDateTime: String, // "2026-11-20T18:00:00"
)

// ---------- Séances (session-service) ----------

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
    val kind: String, // "PDF" ou "LINK"
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
data class SeanceReponse(
    val exists: Boolean = false,
    val role: String = "REQUESTER", // PROVIDER | REQUESTER | MODERATOR
    val canEdit: Boolean = false,
    val canAddResources: Boolean = false,
    val completeness: Completude = Completude(),
    val session: SeanceContenu = SeanceContenu(),
    val resources: List<Support> = emptyList(),
)

@Serializable data class LienRequest(val title: String, val url: String)

@Serializable data class ResumeSeance(val exists: Boolean = false, val percent: Int = 0, val resources: Int = 0)

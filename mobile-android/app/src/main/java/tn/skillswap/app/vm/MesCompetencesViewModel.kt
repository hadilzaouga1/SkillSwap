package tn.skillswap.app.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tn.skillswap.app.data.*

val NIVEAUX = listOf("DEBUTANT" to "Débutant", "INTERMEDIAIRE" to "Intermédiaire", "AVANCE" to "Avancé")

/** Retourne un message d'erreur, ou null si la compétence est valide (mêmes règles que le serveur). */
fun validerCompetence(titre: String, description: String, categorieId: Long?, niveau: String?): String? = when {
    titre.isBlank() -> "Le titre est obligatoire."
    titre.trim().length > 120 -> "Le titre ne doit pas dépasser 120 caractères."
    description.length > 2000 -> "La description ne doit pas dépasser 2000 caractères."
    categorieId == null -> "Choisissez une catégorie."
    niveau == null -> "Choisissez un niveau."
    else -> null
}

data class MesCompetencesEtat(
    val liste: List<Competence> = emptyList(),
    val categories: List<Categorie> = emptyList(),
    val enChargement: Boolean = true,
    val enregistrement: Boolean = false,
    val erreur: String? = null,
)

class MesCompetencesViewModel(private val api: Api) : ViewModel() {
    private val _etat = MutableStateFlow(MesCompetencesEtat())
    val etat: StateFlow<MesCompetencesEtat> = _etat.asStateFlow()

    init { charger() }

    fun charger() {
        viewModelScope.launch {
            _etat.update { it.copy(enChargement = true, erreur = null) }
            try { val l = api.mesCompetences(); _etat.update { it.copy(liste = l) } }
            catch (e: Exception) { _etat.update { it.copy(erreur = e.messageUtilisateur()) } }
            try { val c = api.categories(); _etat.update { it.copy(categories = c) } } catch (_: Exception) {}
            _etat.update { it.copy(enChargement = false) }
        }
    }

    /** Crée (id == null) ou modifie une compétence. [onOk] est appelé après un succès. */
    fun enregistrer(id: Long?, titre: String, description: String, niveau: String?, categorieId: Long?, dispos: String, onOk: () -> Unit) {
        validerCompetence(titre, description, categorieId, niveau)?.let { err -> _etat.update { it.copy(erreur = err) }; return }
        val req = CompetenceRequest(titre.trim(), description.trim(), niveau!!, categorieId!!, dispos.trim().ifBlank { null })
        viewModelScope.launch {
            _etat.update { it.copy(enregistrement = true, erreur = null) }
            try {
                if (id == null) api.creerCompetence(req) else api.modifierCompetence(id, req)
                charger()
                onOk()
            } catch (e: Exception) {
                _etat.update { it.copy(erreur = e.messageUtilisateur()) }
            }
            _etat.update { it.copy(enregistrement = false) }
        }
    }

    fun supprimer(id: Long) {
        viewModelScope.launch {
            try { api.supprimerCompetence(id); charger() }
            catch (e: Exception) { _etat.update { it.copy(erreur = e.messageUtilisateur()) } }
        }
    }
}

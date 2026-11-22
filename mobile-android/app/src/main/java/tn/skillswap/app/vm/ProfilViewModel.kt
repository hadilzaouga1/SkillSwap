package tn.skillswap.app.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tn.skillswap.app.data.*

data class ProfilEtat(
    val profil: Profil? = null,
    val reputation: Reputation? = null,
    val solde: Solde? = null,
    val enChargement: Boolean = false,
    val enregistrement: Boolean = false,
    val erreur: String? = null,
    val message: String? = null,
)

/** Retourne un message d'erreur, ou null si le formulaire de profil est valide. */
fun validerProfil(nom: String): String? =
    if (nom.isBlank()) "Le nom est obligatoire." else if (nom.trim().length > 80) "Le nom ne doit pas dépasser 80 caractères." else null

class ProfilViewModel(private val api: Api, private val moiId: Long?) : ViewModel() {
    private val _etat = MutableStateFlow(ProfilEtat())
    val etat: StateFlow<ProfilEtat> = _etat.asStateFlow()

    init { charger() }

    fun charger() {
        viewModelScope.launch {
            _etat.update { it.copy(enChargement = true, erreur = null) }
            try { val p = api.monProfil(); _etat.update { it.copy(profil = p) } }
            catch (e: Exception) { _etat.update { it.copy(erreur = e.messageUtilisateur()) } }
            try { val s = api.solde(); _etat.update { it.copy(solde = s) } } catch (_: Exception) {}
            moiId?.let { id -> try { val r = api.reputation(id); _etat.update { it.copy(reputation = r) } } catch (_: Exception) {} }
            _etat.update { it.copy(enChargement = false) }
        }
    }

    fun enregistrer(nom: String, universite: String, filiere: String) {
        validerProfil(nom)?.let { err -> _etat.update { it.copy(erreur = err, message = null) }; return }
        viewModelScope.launch {
            _etat.update { it.copy(enregistrement = true, erreur = null, message = null) }
            try {
                val p = api.modifierProfil(ProfilUpdate(nom.trim(), universite.trim().ifBlank { null }, filiere.trim().ifBlank { null }))
                _etat.update { it.copy(profil = p, message = "Profil enregistré.") }
            } catch (e: Exception) {
                _etat.update { it.copy(erreur = e.messageUtilisateur()) }
            }
            _etat.update { it.copy(enregistrement = false) }
        }
    }
}

package tn.skillswap.app.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tn.skillswap.app.data.*
import tn.skillswap.app.data.local.CacheStore

data class CatalogueEtat(
    val motCle: String = "",
    val categorieId: Long? = null,
    val niveau: String? = null,
    val categories: List<Categorie> = emptyList(),
    val enChargement: Boolean = false,
    val erreur: String? = null,
    /** Vrai si le dernier rafraîchissement a échoué : la liste vient alors du cache Room. */
    val horsLigne: Boolean = false,
    val grille: Boolean = false,
)

/** Filtre local (mot-clé, catégorie, niveau) : fonctionne aussi hors ligne. */
fun filtrerCompetences(liste: List<Competence>, motCle: String, categorieId: Long?, niveau: String?): List<Competence> {
    val mot = motCle.trim().lowercase()
    return liste.filter { c ->
        (mot.isEmpty() || c.titre.lowercase().contains(mot) || c.description.lowercase().contains(mot)) &&
            (categorieId == null || c.categorie?.id == categorieId) &&
            (niveau == null || c.niveau.equals(niveau, ignoreCase = true))
    }
}

class CatalogueViewModel(private val api: Api, private val cache: CacheStore) : ViewModel() {
    private val _etat = MutableStateFlow(CatalogueEtat())
    val etat: StateFlow<CatalogueEtat> = _etat.asStateFlow()

    val competences: StateFlow<List<Competence>> =
        combine(cache.competences(), _etat) { liste, e -> filtrerCompetences(liste, e.motCle, e.categorieId, e.niveau) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init { rafraichir() }

    fun changerMotCle(v: String) = _etat.update { it.copy(motCle = v) }
    fun choisirCategorie(id: Long?) = _etat.update { it.copy(categorieId = id) }
    fun choisirNiveau(n: String?) = _etat.update { it.copy(niveau = n) }
    fun basculerGrille() = _etat.update { it.copy(grille = !it.grille) }

    fun rafraichir() {
        viewModelScope.launch {
            _etat.update { it.copy(enChargement = true, erreur = null) }
            try {
                cache.remplacerCompetences(api.competences())
                _etat.update { it.copy(horsLigne = false) }
            } catch (e: Exception) {
                _etat.update { it.copy(erreur = e.messageUtilisateur(), horsLigne = true) }
            }
            try {
                val cats = api.categories()
                _etat.update { it.copy(categories = cats) }
            } catch (_: Exception) { /* facultatif : les filtres par catégorie seront simplement absents */ }
            _etat.update { it.copy(enChargement = false) }
        }
    }
}

package tn.skillswap.app.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import tn.skillswap.app.data.*
import tn.skillswap.app.data.local.CacheStore

data class EchangesEtat(
    val enChargement: Boolean = false,
    val erreur: String? = null,
    val horsLigne: Boolean = false,
    val resumes: Map<String, ResumeSeance> = emptyMap(),
)

class EchangesViewModel(
    private val api: Api,
    private val cache: CacheStore,
    /** Notifie les autres participants après une action (voir Repository.signaler). */
    private val signaler: suspend (Long) -> Unit = {},
) : ViewModel() {

    private val _etat = MutableStateFlow(EchangesEtat())
    val etat: StateFlow<EchangesEtat> = _etat.asStateFlow()

    val recues: StateFlow<List<Exchange>> = cache.echanges(RECEIVED).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val envoyees: StateFlow<List<Exchange>> = cache.echanges(SENT).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init { rafraichir() }

    fun rafraichir() {
        viewModelScope.launch {
            _etat.update { it.copy(enChargement = true) }
            try {
                val r = api.recues()
                val e = api.envoyees()
                cache.remplacerEchanges(RECEIVED, r)
                cache.remplacerEchanges(SENT, e)
                _etat.update { it.copy(erreur = null, horsLigne = false) }
                val ids = (r + e).map { it.id }
                if (ids.isNotEmpty()) {
                    // Facultatif : si le session-service est arrêté, la liste reste affichée
                    try { val res = api.resumes(ids.joinToString(",")); _etat.update { it.copy(resumes = res) } } catch (_: Exception) {}
                }
            } catch (ex: Exception) {
                _etat.update { it.copy(erreur = ex.messageUtilisateur(), horsLigne = true) }
            }
            _etat.update { it.copy(enChargement = false) }
        }
    }

    private fun agir(appel: suspend () -> Exchange) {
        viewModelScope.launch {
            try {
                val maj = appel()
                signaler(maj.id)
                rafraichir()
            } catch (ex: Exception) {
                _etat.update { it.copy(erreur = ex.messageUtilisateur()) }
                if (ex is HttpException && ex.code() == 409) rafraichir()
            }
        }
    }

    fun accepter(id: Long) = agir { api.accepter(id) }
    fun refuser(id: Long) = agir { api.refuser(id) }
    fun annuler(id: Long) = agir { api.annuler(id) }
    fun terminer(id: Long) = agir { api.terminer(id) }
    fun confirmer(id: Long) = agir { api.confirmer(id) }
    /** Le prestataire propose un autre créneau (statut TIME_PROPOSED). */
    fun proposerCreneau(id: Long, iso: String) = agir { api.proposerCreneau(id, ProposeTimeRequest(iso)) }
    /** Le demandeur accepte le nouveau créneau proposé. */
    fun accepterCreneau(id: Long) = agir { api.accepterCreneau(id) }

    fun effacerErreur() = _etat.update { it.copy(erreur = null) }

    companion object {
        const val SENT = "SENT"
        const val RECEIVED = "RECEIVED"
    }
}

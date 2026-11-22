package tn.skillswap.app.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tn.skillswap.app.data.*

data class AlertesEtat(
    val items: List<NotificationItem> = emptyList(),
    val nonLues: Int = 0,
    val enChargement: Boolean = true,
    val erreur: String? = null,
)

class AlertesViewModel(private val api: Api) : ViewModel() {
    private val _etat = MutableStateFlow(AlertesEtat())
    val etat: StateFlow<AlertesEtat> = _etat.asStateFlow()

    init { charger() }

    fun charger() {
        viewModelScope.launch {
            try {
                val r = api.notifications()
                _etat.update { it.copy(items = r.items, nonLues = r.unreadCount, erreur = null) }
            } catch (e: Exception) {
                _etat.update { it.copy(erreur = e.messageUtilisateur()) }
            }
            _etat.update { it.copy(enChargement = false) }
        }
    }

    fun toutLire() {
        viewModelScope.launch {
            try { api.toutLire(); charger() } catch (e: Exception) { _etat.update { it.copy(erreur = e.messageUtilisateur()) } }
        }
    }

    fun lire(n: NotificationItem) {
        if (n.read) return
        viewModelScope.launch {
            try { api.lireNotification(n.id) } catch (_: Exception) {}
            charger()
        }
    }
}

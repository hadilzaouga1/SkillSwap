package tn.skillswap.app.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import tn.skillswap.app.data.*

data class CreditsEtat(
    val solde: Solde? = null,
    val transactions: List<TransactionCredit> = emptyList(),
    val enChargement: Boolean = true,
    val erreur: String? = null,
)

class CreditsViewModel(private val api: Api) : ViewModel() {
    private val _etat = MutableStateFlow(CreditsEtat())
    val etat: StateFlow<CreditsEtat> = _etat.asStateFlow()

    init { charger() }

    fun charger() {
        viewModelScope.launch {
            _etat.update { it.copy(enChargement = true, erreur = null) }
            try {
                val s = api.solde()
                val t = api.transactions()
                _etat.update { it.copy(solde = s, transactions = t) }
            } catch (e: Exception) {
                _etat.update { it.copy(erreur = e.messageUtilisateur()) }
            }
            _etat.update { it.copy(enChargement = false) }
        }
    }
}

/** Libellé lisible d'un type de transaction du credit-service. */
fun libelleTransaction(type: String): String = when (type) {
    "WELCOME" -> "Crédits de bienvenue"
    "RESERVATION" -> "Crédits bloqués (échange accepté)"
    "RELEASE" -> "Crédits débloqués"
    "TRANSFER_OUT" -> "Paiement du prestataire"
    "TRANSFER_IN" -> "Crédits reçus"
    else -> type
}

package tn.skillswap.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.abs
import tn.skillswap.app.vm.CreditsViewModel
import tn.skillswap.app.vm.libelleTransaction

/** Types qui diminuent le solde disponible de l'utilisateur. */
private val SORTIES = setOf("RESERVATION", "TRANSFER_OUT")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen(vm: CreditsViewModel, onRetour: () -> Unit) {
    val etat by vm.etat.collectAsStateWithLifecycle()
    Scaffold(topBar = {
        TopAppBar(title = { Text("Mes crédits") },
            navigationIcon = { IconButton(onClick = onRetour) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") } })
    }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)) {
            item {
                if (etat.enChargement) LinearProgressIndicator(Modifier.fillMaxWidth())
                MessageErreur(etat.erreur)
                etat.solde?.let { s ->
                    Carte {
                        Text("Solde disponible", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${s.available} h", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary)
                        Text("Total : ${s.balance} h · bloqué : ${s.reserved} h", style = MaterialTheme.typography.bodySmall)
                        Text("1 heure de service = 1 crédit", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Historique", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (!etat.enChargement && etat.transactions.isEmpty() && etat.erreur == null)
                    Text("Aucune transaction pour le moment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(etat.transactions, key = { it.id }) { t ->
                val sortie = t.type in SORTIES
                Carte {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(libelleTransaction(t.type), fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text((if (sortie) "−" else "+") + "${abs(t.amount)} h", fontWeight = FontWeight.Bold,
                            color = if (sortie) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    }
                    t.description?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Text(t.createdAt.replace('T', ' ').take(16), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

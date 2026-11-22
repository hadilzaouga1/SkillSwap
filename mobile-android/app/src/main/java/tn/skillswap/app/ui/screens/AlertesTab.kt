package tn.skillswap.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tn.skillswap.app.vm.AlertesViewModel

@Composable
fun AlertesTab(vm: AlertesViewModel, onSeance: (Long) -> Unit, refresh: Int) {
    val etat by vm.etat.collectAsStateWithLifecycle()
    LaunchedEffect(refresh) { if (refresh > 0) vm.charger() }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Notifications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = vm::toutLire) { Text("Tout marquer lu") }
        }
        MessageErreur(etat.erreur)
        if (etat.enChargement) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (!etat.enChargement && etat.items.isEmpty() && etat.erreur == null)
            Text("Aucune notification pour le moment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
            items(etat.items, key = { it.id }) { n ->
                Carte(Modifier.clickable { vm.lire(n); n.exchangeId?.let(onSeance) }) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!n.read) Etiquette("Nouveau")
                        Text(n.title, fontWeight = if (n.read) FontWeight.Normal else FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    }
                    Text(n.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(n.createdAt.replace('T', ' ').take(16), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

package tn.skillswap.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tn.skillswap.app.data.*

/** Notifications (notification-service) : liste, lecture au toucher, "tout marquer comme lu". */
@Composable
fun AlertesTab(repo: Repository, onSeance: (Long) -> Unit, refresh: Int, onNonLues: (Int) -> Unit) {
    var liste by remember { mutableStateOf<List<NotificationItem>>(emptyList()) }
    var erreur by remember { mutableStateOf<String?>(null) }
    var charge by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun charger() {
        scope.launch {
            try { val r = repo.api.notifications(); liste = r.items; onNonLues(r.unreadCount); erreur = null }
            catch (e: Exception) { erreur = e.messageUtilisateur() }
            charge = false
        }
    }
    LaunchedEffect(refresh) { charger() }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Notifications", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = { scope.launch { try { repo.api.toutLire(); charger() } catch (e: Exception) { erreur = e.messageUtilisateur() } } }) {
                Text("Tout marquer lu")
            }
        }
        MessageErreur(erreur)
        if (charge) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (!charge && liste.isEmpty() && erreur == null) Text("Aucune notification pour le moment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
            items(liste, key = { it.id }) { n ->
                Carte(Modifier.clickable {
                    scope.launch {
                        if (!n.read) try { repo.api.lireNotification(n.id) } catch (_: Exception) {}
                        charger()
                        n.exchangeId?.let(onSeance)
                    }
                }) {
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

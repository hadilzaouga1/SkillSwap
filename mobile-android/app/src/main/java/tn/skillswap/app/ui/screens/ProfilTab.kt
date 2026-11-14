package tn.skillswap.app.ui.screens

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

/** Profil : identité, réputation (trust-service), avis reçus, déconnexion. */
@Composable
fun ProfilTab(repo: Repository, onDeconnexion: () -> Unit, refresh: Int) {
    val moi = repo.utilisateur
    var rep by remember { mutableStateOf<Reputation?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(refresh) {
        moi?.let { try { rep = repo.api.reputation(it.id) } catch (_: Exception) {} }
    }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)) {
        item {
            Carte {
                Text(moi?.nom.orEmpty(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Etiquette(moi?.role.orEmpty())
                val r = rep
                if (r == null || r.count == 0) Text("Pas encore d'avis reçus.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                else {
                    Text("★ ${"%.1f".format(r.average)} / 5  (${r.count} avis)", style = MaterialTheme.typography.titleMedium)
                    r.badge?.let { Etiquette("🏅 ${it.label}") }
                }
                OutlinedButton(onClick = { scope.launch { repo.deconnexion(); onDeconnexion() } }, modifier = Modifier.fillMaxWidth()) {
                    Text("Se déconnecter")
                }
            }
        }
        item { Text("Avis reçus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        items(rep?.reviews.orEmpty(), key = { it.id }) { a ->
            Carte {
                Text("★".repeat(a.rating) + "☆".repeat(5 - a.rating))
                a.comment?.let { Text(it) }
                a.skillTitle?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

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

/** Catalogue : liste défilante (LazyColumn = l'équivalent Compose de RecyclerView/ListView). */
@Composable
fun CatalogueTab(repo: Repository, onCompetence: (Long) -> Unit, refresh: Int) {
    var motCle by remember { mutableStateOf("") }
    var liste by remember { mutableStateOf<List<Competence>>(emptyList()) }
    var erreur by remember { mutableStateOf<String?>(null) }
    var charge by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun charger() {
        charge = true; erreur = null
        scope.launch {
            try { liste = repo.api.competences(motCle.trim().ifBlank { null }) }
            catch (e: Exception) { erreur = e.messageUtilisateur() }
            charge = false
        }
    }
    LaunchedEffect(refresh) { charger() }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(motCle, { motCle = it }, singleLine = true, label = { Text("Rechercher une compétence") },
            modifier = Modifier.fillMaxWidth(), trailingIcon = { TextButton(onClick = { charger() }) { Text("OK") } })
        MessageErreur(erreur)
        if (charge) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (!charge && liste.isEmpty() && erreur == null) Text("Aucune compétence trouvée.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            items(liste, key = { it.id }) { c ->
                Carte(Modifier.clickable { onCompetence(c.id) }) {
                    Text(c.titre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        c.categorie?.let { Etiquette(it.nom) }
                        if (c.niveau.isNotBlank()) Etiquette(c.niveau.lowercase().replaceFirstChar { it.uppercase() })
                    }
                    Text(c.description, maxLines = 3, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Voir le détail →", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

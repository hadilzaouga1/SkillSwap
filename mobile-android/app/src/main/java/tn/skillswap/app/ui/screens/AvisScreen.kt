package tn.skillswap.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tn.skillswap.app.data.*

/** Avis sur un échange terminé : étoiles, commentaire (reformulable par l'IA), avis déjà donnés. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvisScreen(repo: Repository, exchangeId: Long, titre: String, onRetour: () -> Unit, onPublie: () -> Unit) {
    val scope = rememberCoroutineScope()
    var data by remember { mutableStateOf<AvisEchange?>(null) }
    var note by remember { mutableIntStateOf(0) }
    var commentaire by remember { mutableStateOf("") }
    var conseils by remember { mutableStateOf<List<String>>(emptyList()) }
    var erreur by remember { mutableStateOf<String?>(null) }
    var occupe by remember { mutableStateOf(false) }

    fun charger() = scope.launch {
        try { data = repo.api.avisEchange(exchangeId) } catch (e: Exception) { erreur = e.messageUtilisateur() }
    }
    LaunchedEffect(exchangeId) { charger() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text("Avis") },
                navigationIcon = { IconButton(onClick = onRetour) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
        },
    ) { pad ->
        Column(Modifier.padding(pad).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(titre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            MessageErreur(erreur)
            val d = data
            if (d == null) { if (erreur == null) LinearProgressIndicator(Modifier.fillMaxWidth()) }
            else {
                if (d.canReview) Carte {
                    Text("Votre note", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { n ->
                            Text(if (n <= note) "★" else "☆", style = MaterialTheme.typography.headlineLarge,
                                color = if (n <= note) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.clickable { note = n })
                        }
                    }
                    OutlinedTextField(commentaire, { commentaire = it.take(500) }, label = { Text("Commentaire (facultatif)") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                    TextButton(enabled = commentaire.trim().length >= 3 && !occupe, onClick = {
                        occupe = true
                        scope.launch {
                            try { val r = repo.api.iaAmeliorer(IaImproveRequest(commentaire, "review")); commentaire = r.text.take(500); conseils = r.tips }
                            catch (e: Exception) { erreur = e.messageUtilisateur() }
                            occupe = false
                        }
                    }) { Text("✨ Reformuler avec l'IA") }
                    conseils.forEach { Text("💡 $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Button(enabled = note > 0 && !occupe, modifier = Modifier.fillMaxWidth(), onClick = {
                        occupe = true; erreur = null
                        scope.launch {
                            try { repo.api.publierAvis(exchangeId, AvisRequest(note, commentaire.trim().ifBlank { null })); onPublie() }
                            catch (e: Exception) { erreur = e.messageUtilisateur() }
                            occupe = false
                        }
                    }) { Text("Publier mon avis") }
                }
                d.mine?.let { Carte { Text("Votre avis", fontWeight = FontWeight.Bold); Text("★".repeat(it.rating) + "☆".repeat(5 - it.rating)); it.comment?.let { c -> Text(c) } } }
                d.theirs?.let { Carte { Text("Avis reçu", fontWeight = FontWeight.Bold); Text("★".repeat(it.rating) + "☆".repeat(5 - it.rating)); it.comment?.let { c -> Text(c) } } }
                if (!d.canReview && d.mine == null) Text("Les avis seront possibles une fois l'échange terminé.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

package tn.skillswap.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tn.skillswap.app.data.*

private data class Bulle(val auteur: String, val texte: String, val competences: List<IaCompetence> = emptyList())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(repo: Repository, page: String, texteInitial: String?, onRetour: () -> Unit, onCompetence: (Long) -> Unit) {
    val bulles = remember { mutableStateListOf<Bulle>() }
    var saisie by remember { mutableStateOf(texteInitial.orEmpty()) }
    var suggestions by remember { mutableStateOf(listOf("Comment fonctionnent les crédits ?", "Je veux apprendre Java", "Comment demander un échange ?")) }
    var occupe by remember { mutableStateOf(false) }
    var erreur by remember { mutableStateOf<String?>(null) }
    val etat = rememberLazyListState()
    val scope = rememberCoroutineScope()

    fun envoyer(texte: String) {
        val message = texte.trim()
        if (message.isEmpty() || occupe) return
        val historique = bulles.takeLast(10).map { IaMsg(if (it.auteur == "moi") "user" else "assistant", it.texte) }
        bulles.add(Bulle("moi", message)); saisie = ""; occupe = true; erreur = null
        scope.launch {
            try {
                val r = repo.api.iaChat(IaChatRequest(message, historique, IaContext(page)))
                bulles.add(Bulle("ia", r.reply, r.skills)); suggestions = r.suggestions
            } catch (e: Exception) { erreur = e.messageUtilisateur() }
            occupe = false
            if (bulles.isNotEmpty()) etat.animateScrollToItem(bulles.lastIndex)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text("Assistant SkillSwap ✨") },
                navigationIcon = { IconButton(onClick = onRetour) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
        },
        bottomBar = {
            Column(Modifier.navigationBarsPadding().imePadding().padding(8.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(suggestions) { s -> AssistChip(onClick = { envoyer(s) }, label = { Text(s) }) }
                }
                Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(saisie, { saisie = it.take(1000) }, modifier = Modifier.weight(1f), singleLine = true, placeholder = { Text("Écris ta question…") })
                    Button(onClick = { envoyer(saisie) }, enabled = saisie.isNotBlank() && !occupe) { Text("Envoyer") }
                }
            }
        },
    ) { pad ->
        LazyColumn(Modifier.padding(pad).padding(horizontal = 16.dp).fillMaxSize(), state = etat,
            verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 8.dp)) {
            if (bulles.isEmpty()) item {
                Text("Salut ${repo.utilisateur?.nom.orEmpty()} 👋 Pose-moi une question ou dis-moi ce que tu veux apprendre.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(bulles) { b ->
                val moi = b.auteur == "moi"
                Column(Modifier.fillMaxWidth(), horizontalAlignment = if (moi) Alignment.End else Alignment.Start) {
                    Surface(shape = RoundedCornerShape(18.dp), shadowElevation = 1.dp,
                        color = if (moi) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface) {
                        Text(b.texte, Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            color = if (moi) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                    }
                    b.competences.forEach { c ->
                        Carte(Modifier.padding(top = 6.dp).clickable { onCompetence(c.id) }) {
                            Text(c.titre, fontWeight = FontWeight.Bold)
                            Text("${c.categorie.orEmpty()} · ${c.niveau.orEmpty()} — Demander un échange →",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            if (occupe) item { Text("L'assistant réfléchit…", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            erreur?.let { e -> item { MessageErreur(e) } }
        }
    }
    LaunchedEffect(Unit) { if (!texteInitial.isNullOrBlank()) envoyer(texteInitial) }
}

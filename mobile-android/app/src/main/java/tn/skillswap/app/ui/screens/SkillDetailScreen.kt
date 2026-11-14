package tn.skillswap.app.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tn.skillswap.app.data.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Détail d'une compétence + formulaire de demande d'échange (durée, date, message avec aide de l'IA). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillDetailScreen(repo: Repository, skillId: Long, onRetour: () -> Unit, onEnvoye: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var c by remember { mutableStateOf<Competence?>(null) }
    var rep by remember { mutableStateOf<Reputation?>(null) }
    var erreur by remember { mutableStateOf<String?>(null) }
    var duree by remember { mutableIntStateOf(1) }
    var date by remember { mutableStateOf<LocalDateTime?>(null) }
    var message by remember { mutableStateOf("") }
    var conseils by remember { mutableStateOf<List<String>>(emptyList()) }
    var occupe by remember { mutableStateOf(false) }

    LaunchedEffect(skillId) {
        try {
            c = repo.api.competence(skillId)
            c?.etudiantId?.let { id -> try { rep = repo.api.reputation(id) } catch (_: Exception) {} }
        } catch (e: Exception) { erreur = e.messageUtilisateur() }
    }

    /** Boîtes de dialogue Android classiques : d'abord la date, puis l'heure. */
    fun choisirDate() {
        val base = date ?: LocalDateTime.now().plusDays(2).withMinute(0)
        DatePickerDialog(ctx, { _, a, m, j ->
            TimePickerDialog(ctx, { _, h, min -> date = LocalDateTime.of(a, m + 1, j, h, min) }, base.hour, 0, true).show()
        }, base.year, base.monthValue - 1, base.dayOfMonth).show()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text("Compétence") },
                navigationIcon = { IconButton(onClick = onRetour) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
        },
    ) { pad ->
        Column(Modifier.padding(pad).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MessageErreur(erreur)
            val comp = c
            if (comp == null) { if (erreur == null) LinearProgressIndicator(Modifier.fillMaxWidth()) }
            else {
                Carte {
                    Text(comp.titre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        comp.categorie?.let { Etiquette(it.nom) }
                        if (comp.niveau.isNotBlank()) Etiquette(comp.niveau.lowercase().replaceFirstChar { it.uppercase() })
                    }
                    Text(comp.description)
                    comp.disponibilites?.let { Text("Disponibilités : $it", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    rep?.takeIf { it.count > 0 }?.let {
                        Text("★ ${"%.1f".format(it.average)} (${it.count} avis)" + (it.badge?.let { b -> " · 🏅 ${b.label}" } ?: ""),
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
                if (comp.etudiantId == repo.utilisateur?.id) {
                    Text("C'est votre propre compétence : vous ne pouvez pas la demander.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else Carte {
                    Text("Demander un échange", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Durée : $duree h")
                        OutlinedButton(onClick = { if (duree > 1) duree-- }) { Text("−") }
                        OutlinedButton(onClick = { if (duree < 8) duree++ }) { Text("+") }
                    }
                    OutlinedButton(onClick = { choisirDate() }, modifier = Modifier.fillMaxWidth()) {
                        Text(date?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH:mm")) ?: "Choisir la date et l'heure")
                    }
                    OutlinedTextField(message, { message = it.take(1000) }, label = { Text("Message au prestataire") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                    TextButton(enabled = message.trim().length >= 3 && !occupe, onClick = {
                        occupe = true
                        scope.launch {
                            try { val r = repo.api.iaAmeliorer(IaImproveRequest(message, "request")); message = r.text.take(1000); conseils = r.tips }
                            catch (e: Exception) { erreur = e.messageUtilisateur() }
                            occupe = false
                        }
                    }) { Text("✨ Améliorer mon message avec l'IA") }
                    conseils.forEach { Text("💡 $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Button(enabled = date != null && !occupe, modifier = Modifier.fillMaxWidth(), onClick = {
                        occupe = true; erreur = null
                        scope.launch {
                            try {
                                val iso = date!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                                val e = repo.api.creerEchange(ExchangeRequest(comp.id, message.trim(), duree, iso))
                                repo.signaler(e.id)   // notifie le prestataire
                                onEnvoye()
                            } catch (e: Exception) { erreur = e.messageUtilisateur() }
                            occupe = false
                        }
                    }) { Text("Envoyer la demande") }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

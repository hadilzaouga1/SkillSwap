package tn.skillswap.app.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import tn.skillswap.app.data.*

/**
 * Fiche complète d'une séance : description, objectifs, programme, public, modalités,
 * et supports (PDF + liens). Le prestataire peut modifier les champs principaux.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeanceScreen(repo: Repository, exchangeId: Long, onRetour: () -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var data by remember { mutableStateOf<SeanceReponse?>(null) }
    var erreur by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }
    var edition by remember { mutableStateOf(false) }

    fun charger() = scope.launch {
        try { data = repo.api.seance(exchangeId); erreur = null }
        catch (e: Exception) { erreur = e.messageUtilisateur() }
    }
    LaunchedEffect(exchangeId) { charger() }

    // Sélecteur de PDF du téléphone
    val choisirPdf = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) scope.launch {
            try {
                val octets = ctx.contentResolver.openInputStream(uri)!!.use { it.readBytes() }
                val nom = uri.lastPathSegment?.substringAfterLast('/') ?: "document.pdf"
                repo.envoyerPdf(exchangeId, octets, if (nom.endsWith(".pdf", true)) nom else "$nom.pdf", nom.removeSuffix(".pdf"))
                info = "PDF ajouté."; charger()
            } catch (e: Exception) { erreur = e.messageUtilisateur() }
        }
    }

    fun ouvrirPdf(s: Support) = scope.launch {
        try {
            val f = repo.telechargerPdf(exchangeId, s)
            val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.files", f)
            ctx.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/pdf")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) { erreur = "Aucune application de lecture PDF installée." }
        catch (e: Exception) { erreur = e.messageUtilisateur() }
    }

    fun ouvrirLien(url: String) {
        try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        catch (_: Exception) { erreur = "Impossible d'ouvrir ce lien." }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Fiche de séance") },
                navigationIcon = { IconButton(onClick = onRetour) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { pad ->
        Column(Modifier.padding(pad).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MessageErreur(erreur)
            info?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            val d = data
            if (d == null) { if (erreur == null) LinearProgressIndicator(Modifier.fillMaxWidth()) }
            else {
                val c = d.session
                Carte {
                    Text(c.title.ifBlank { "Séance sans titre" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                    c.summary?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        c.format?.let { Etiquette(FORMATS[it] ?: it) }
                        c.mode?.let { Etiquette(MODES[it] ?: it) }
                    }
                    LinearProgressIndicator(progress = { d.completeness.percent / 100f }, Modifier.fillMaxWidth())
                    Text("Fiche complétée à ${d.completeness.percent} %", style = MaterialTheme.typography.labelMedium)
                    if (d.completeness.missing.isNotEmpty())
                        Text("À compléter : " + d.completeness.missing.joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (d.canEdit && !edition) Button(onClick = { edition = true }) { Text(if (d.exists) "Modifier la fiche" else "Décrire la séance") }
                }

                if (edition) FormulaireEdition(c, onAnnuler = { edition = false }) { nouveau ->
                    scope.launch {
                        try { data = repo.api.enregistrerSeance(exchangeId, nouveau); edition = false; info = "Fiche enregistrée." ; erreur = null }
                        catch (e: Exception) { erreur = e.messageUtilisateur() }
                    }
                } else {
                    if (c.description != null || c.objectives.isNotEmpty()) Carte {
                        c.description?.let { Titre("À propos du cours"); Text(it) }
                        if (c.objectives.isNotEmpty()) { Titre("Objectifs"); c.objectives.forEach { Text("✓ $it") } }
                    }
                    if (c.outline.isNotEmpty()) Carte {
                        Titre("Programme")
                        c.outline.forEachIndexed { i, e -> Text("${i + 1}. ${e.title}" + (e.minutes?.let { " ($it min)" } ?: "")) }
                    }
                    Carte {
                        Titre("Public et déroulement")
                        Ligne("Public visé", c.audience); Ligne("Prérequis", c.prerequisites); Ligne("Langue", c.language)
                        Ligne("Matériel", c.materials); Ligne("Plateforme", c.platform); Ligne("Lieu", c.location)
                        Ligne("Comment la séance sera dispensée", c.deliveryNotes)
                        c.meetingLink?.let { l -> OutlinedButton(onClick = { ouvrirLien(l) }) { Text("Rejoindre la réunion") } }
                    }
                }

                Carte {
                    Titre("Supports (${d.resources.size})")
                    if (d.resources.isEmpty()) Text("Aucun support pour le moment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    d.resources.forEach { s ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text((if (s.kind == "PDF") "📄 " else "🔗 ") + s.title, fontWeight = FontWeight.SemiBold)
                                Text(if (s.kind == "PDF") "PDF · ${s.fileName.orEmpty()}" else s.host.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(onClick = { if (s.kind == "PDF") ouvrirPdf(s) else s.url?.let(::ouvrirLien) }) { Text("Ouvrir") }
                            if (d.role == "PROVIDER" || s.addedBy == repo.utilisateur?.id)
                                TextButton(onClick = {
                                    scope.launch {
                                        try { repo.api.supprimerSupport(exchangeId, s.id); charger() }
                                        catch (e: Exception) { erreur = e.messageUtilisateur() }
                                    }
                                }) { Text("Supprimer") }
                        }
                    }
                    if (d.canAddResources) {
                        HorizontalDivider()
                        var titre by remember { mutableStateOf("") }
                        var url by remember { mutableStateOf("") }
                        OutlinedTextField(titre, { titre = it }, label = { Text("Titre du lien") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(url, { url = it }, label = { Text("https://…") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(enabled = titre.isNotBlank() && url.isNotBlank(), onClick = {
                                scope.launch {
                                    try { repo.api.ajouterLien(exchangeId, LienRequest(titre.trim(), url.trim())); titre = ""; url = ""; info = "Lien ajouté."; charger() }
                                    catch (e: Exception) { erreur = e.messageUtilisateur() }
                                }
                            }) { Text("Ajouter le lien") }
                            OutlinedButton(onClick = { choisirPdf.launch("application/pdf") }) { Text("Ajouter un PDF") }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable private fun Titre(t: String) = Text(t, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

@Composable
private fun Ligne(label: String, valeur: String?) {
    if (valeur.isNullOrBlank()) return
    Column { Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(valeur) }
}

/** Formulaire du prestataire. Les champs non affichés (programme, niveau…) sont conservés tels quels. */
@Composable
private fun FormulaireEdition(c: SeanceContenu, onAnnuler: () -> Unit, onEnregistrer: (SeanceContenu) -> Unit) {
    var titre by remember { mutableStateOf(c.title) }
    var resume by remember { mutableStateOf(c.summary.orEmpty()) }
    var description by remember { mutableStateOf(c.description.orEmpty()) }
    var objectifs by remember { mutableStateOf(c.objectives.joinToString("\n")) }
    var audience by remember { mutableStateOf(c.audience.orEmpty()) }
    var mode by remember { mutableStateOf(c.mode) }
    var plateforme by remember { mutableStateOf(c.platform.orEmpty()) }
    var lien by remember { mutableStateOf(c.meetingLink.orEmpty()) }
    var lieu by remember { mutableStateOf(c.location.orEmpty()) }
    var deroulement by remember { mutableStateOf(c.deliveryNotes.orEmpty()) }

    Carte {
        Titre("Décrire la séance")
        OutlinedTextField(titre, { titre = it }, label = { Text("Titre *") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(resume, { resume = it }, label = { Text("Résumé") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(description, { description = it }, label = { Text("Description complète") }, minLines = 4, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(objectifs, { objectifs = it }, label = { Text("Objectifs (un par ligne)") }, minLines = 3, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(audience, { audience = it }, label = { Text("Public visé") }, modifier = Modifier.fillMaxWidth())
        Text("Mode", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MODES.forEach { (k, v) -> FilterChip(selected = mode == k, onClick = { mode = k }, label = { Text(v) }) }
        }
        OutlinedTextField(plateforme, { plateforme = it }, label = { Text("Plateforme (Meet, Zoom…)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(lien, { lien = it }, label = { Text("Lien de la réunion") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(lieu, { lieu = it }, label = { Text("Lieu (présentiel)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(deroulement, { deroulement = it }, label = { Text("Comment la séance sera dispensée") }, minLines = 3, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(enabled = titre.trim().length >= 3, onClick = {
                fun n(s: String) = s.trim().ifBlank { null }
                onEnregistrer(c.copy(
                    title = titre.trim(), summary = n(resume), description = n(description),
                    objectives = objectifs.lines().map { it.trim() }.filter { it.isNotEmpty() },
                    audience = n(audience), mode = mode, platform = n(plateforme), meetingLink = n(lien),
                    location = n(lieu), deliveryNotes = n(deroulement),
                ))
            }) { Text("Enregistrer") }
            TextButton(onClick = onAnnuler) { Text("Annuler") }
        }
    }
}

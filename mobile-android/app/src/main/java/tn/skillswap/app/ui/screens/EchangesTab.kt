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
import retrofit2.HttpException
import tn.skillswap.app.data.*

@Composable
fun EchangesTab(repo: Repository, actions: HomeActions, refresh: Int) {
    var recues by remember { mutableStateOf<List<Exchange>>(emptyList()) }
    var envoyees by remember { mutableStateOf<List<Exchange>>(emptyList()) }
    var resumes by remember { mutableStateOf<Map<String, ResumeSeance>>(emptyMap()) }
    var sousOnglet by remember { mutableIntStateOf(0) }
    var erreur by remember { mutableStateOf<String?>(null) }
    var charge by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val moi = repo.utilisateur?.id

    fun charger() {
        charge = true
        scope.launch {
            try {
                recues = repo.api.recues(); envoyees = repo.api.envoyees(); erreur = null
                val ids = (recues + envoyees).map { it.id }
                if (ids.isNotEmpty()) {
                    // Facultatif : si le session-service est arrêté, la liste reste affichée
                    try { resumes = repo.api.resumes(ids.joinToString(",")) } catch (_: Exception) {}
                }
            } catch (e: Exception) { erreur = e.messageUtilisateur() }
            charge = false
        }
    }
    LaunchedEffect(refresh) { charger() }

    fun action(appel: suspend () -> Exchange) = scope.launch {
        try { val maj = appel(); repo.signaler(maj.id); charger() }
        catch (e: Exception) { erreur = e.messageUtilisateur(); if (e is HttpException && e.code() == 409) charger() }
    }

    Column(Modifier.fillMaxSize()) {
        TabRow(sousOnglet) {
            Tab(sousOnglet == 0, { sousOnglet = 0 }, text = { Text("Reçues (${recues.size})") })
            Tab(sousOnglet == 1, { sousOnglet = 1 }, text = { Text("Envoyées (${envoyees.size})") })
        }
        if (charge) LinearProgressIndicator(Modifier.fillMaxWidth())
        MessageErreur(erreur)
        val liste = if (sousOnglet == 0) recues else envoyees
        if (!charge && liste.isEmpty()) Text("Aucun échange pour le moment.", Modifier.padding(16.dp))
        LazyColumn(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)) {
            items(liste, key = { it.id }) { e ->
                val prestataire = e.providerId == moi
                Carte {
                    Text(e.skillTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Etiquette(STATUTS[e.status] ?: e.status)
                        Etiquette("${e.durationHours} h")
                    }
                    Text(e.proposedDateTime.replace('T', ' ').take(16), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    resumes[e.id.toString()]?.let {
                        Text("Fiche de séance : ${it.percent} % · ${it.resources} support(s)",
                            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (prestataire && e.status == "PENDING") {
                            Button(onClick = { action { repo.api.accepter(e.id) } }) { Text("Accepter") }
                            OutlinedButton(onClick = { action { repo.api.refuser(e.id) } }) { Text("Refuser") }
                        }
                        if (prestataire && e.status == "ACCEPTED") Button(onClick = { action { repo.api.terminer(e.id) } }) { Text("Terminée") }
                        if (!prestataire && e.status == "AWAITING_CONFIRMATION") Button(onClick = { action { repo.api.confirmer(e.id) } }) { Text("Confirmer") }
                    }
                    if (e.status in listOf("PENDING", "TIME_PROPOSED", "ACCEPTED"))
                        TextButton(onClick = { action { repo.api.annuler(e.id) } }) { Text("Annuler l'échange") }
                    if (e.status != "REJECTED" && e.status != "CANCELLED")
                        FilledTonalButton(onClick = { actions.ouvrirSeance(e.id) }, modifier = Modifier.fillMaxWidth()) { Text("Fiche de la séance") }
                    if (e.status == "ACCEPTED")
                        OutlinedButton(onClick = { actions.ajouterAuCalendrier(e) }, modifier = Modifier.fillMaxWidth()) { Text("Ajouter à mon calendrier") }
                    if (e.status == "COMPLETED")
                        OutlinedButton(onClick = { actions.donnerAvis(e) }, modifier = Modifier.fillMaxWidth()) { Text("⭐ Avis sur cet échange") }
                }
            }
        }
    }
}

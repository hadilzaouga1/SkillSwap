package tn.skillswap.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tn.skillswap.app.data.Exchange
import tn.skillswap.app.data.Repository
import tn.skillswap.app.ui.vmDe
import tn.skillswap.app.vm.EchangesViewModel

@Composable
fun EchangesTab(repo: Repository, actions: HomeActions, refresh: Int) {
    val vm = vmDe { EchangesViewModel(repo.api, repo.cache, repo::signaler) }
    val etat by vm.etat.collectAsStateWithLifecycle()
    val recues by vm.recues.collectAsStateWithLifecycle()
    val envoyees by vm.envoyees.collectAsStateWithLifecycle()
    var sousOnglet by rememberSaveable { mutableIntStateOf(0) }
    var erreurLocale by remember { mutableStateOf<String?>(null) }
    val ctx = LocalContext.current
    val moi = repo.utilisateur?.id
    LaunchedEffect(refresh) { if (refresh > 0) vm.rafraichir() }

    Column(Modifier.fillMaxSize()) {
        TabRow(sousOnglet) {
            Tab(sousOnglet == 0, { sousOnglet = 0 }, text = { Text("Reçues (${recues.size})") })
            Tab(sousOnglet == 1, { sousOnglet = 1 }, text = { Text("Envoyées (${envoyees.size})") })
        }
        if (etat.enChargement) LinearProgressIndicator(Modifier.fillMaxWidth())
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            BandeauHorsLigne(etat.horsLigne)
            MessageErreur(erreurLocale ?: etat.erreur.takeIf { !etat.horsLigne })
        }
        val liste = if (sousOnglet == 0) recues else envoyees
        if (!etat.enChargement && liste.isEmpty()) Text("Aucun échange pour le moment.", Modifier.padding(16.dp))
        LazyColumn(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)) {
            items(liste, key = { it.id }) { e ->
                val prestataire = e.providerId == moi
                CarteEchange(e, prestataire, etat.resumes[e.id.toString()], vm, actions, onProposer = {
                    erreurLocale = null
                    choisirDateHeure(ctx, onChoix = { vm.proposerCreneau(e.id, it.versIso()) },
                        onPasse = { erreurLocale = "Le créneau doit être dans le futur." })
                })
            }
        }
    }
}

@Composable
private fun CarteEchange(
    e: Exchange, prestataire: Boolean, resume: tn.skillswap.app.data.ResumeSeance?,
    vm: EchangesViewModel, actions: HomeActions, onProposer: () -> Unit,
) {
    Carte {
        Text(e.skillTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Etiquette(STATUTS[e.status] ?: e.status)
            Etiquette("${e.durationHours} h")
        }
        Text(e.proposedDateTime.replace('T', ' ').take(16), color = MaterialTheme.colorScheme.onSurfaceVariant)
        resume?.let {
            Text("Fiche de séance : ${it.percent} % · ${it.resources} support(s)",
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (prestataire && e.status == "PENDING") {
                Button(onClick = { vm.accepter(e.id) }) { Text("Accepter") }
                OutlinedButton(onClick = { vm.refuser(e.id) }) { Text("Refuser") }
            }
            if (prestataire && e.status == "ACCEPTED") Button(onClick = { vm.terminer(e.id) }) { Text("Terminée") }
            if (!prestataire && e.status == "AWAITING_CONFIRMATION") Button(onClick = { vm.confirmer(e.id) }) { Text("Confirmer") }
        }

        if (prestataire && e.status == "PENDING")
            OutlinedButton(onClick = onProposer, modifier = Modifier.fillMaxWidth()) { Text("Proposer un autre créneau") }
        if (!prestataire && e.status == "TIME_PROPOSED") {
            Text("Le prestataire propose le ${e.proposedDateTime.replace('T', ' ').take(16)}.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Button(onClick = { vm.accepterCreneau(e.id) }, modifier = Modifier.fillMaxWidth()) { Text("Accepter le nouveau créneau") }
        }
        if (e.status in listOf("PENDING", "TIME_PROPOSED", "ACCEPTED"))
            TextButton(onClick = { vm.annuler(e.id) }) { Text("Annuler l'échange") }
        val lien = e.meetingLink
        if (!lien.isNullOrBlank() && e.status in listOf("ACCEPTED", "AWAITING_CONFIRMATION"))
            FilledTonalButton(onClick = { actions.ouvrirLien(lien) }, modifier = Modifier.fillMaxWidth()) { Text("Rejoindre la visio") }
        if (e.status != "REJECTED" && e.status != "CANCELLED")
            FilledTonalButton(onClick = { actions.ouvrirSeance(e.id) }, modifier = Modifier.fillMaxWidth()) { Text("Fiche de la séance") }
        if (e.status == "ACCEPTED")
            OutlinedButton(onClick = { actions.ajouterAuCalendrier(e) }, modifier = Modifier.fillMaxWidth()) { Text("Ajouter à mon calendrier") }
        if (e.status == "COMPLETED")
            OutlinedButton(onClick = { actions.donnerAvis(e) }, modifier = Modifier.fillMaxWidth()) { Text("⭐ Avis sur cet échange") }
    }
}

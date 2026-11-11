package tn.skillswap.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import retrofit2.HttpException
import tn.skillswap.app.data.*

/** Accueil : deux onglets (Catalogue des compétences / Mes échanges). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(repo: Repository, onSeance: (Long) -> Unit, onDeconnexion: () -> Unit) {
    var onglet by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("SkillSwap", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    Text(repo.utilisateur?.nom.orEmpty(), style = MaterialTheme.typography.labelLarge)
                    IconButton(onClick = { scope.launch { repo.deconnexion(); onDeconnexion() } }) {
                        Icon(Icons.Filled.Logout, contentDescription = "Se déconnecter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(onglet == 0, { onglet = 0 }, { Icon(Icons.Filled.School, null) }, label = { Text("Catalogue") })
                NavigationBarItem(onglet == 1, { onglet = 1 }, { Icon(Icons.Filled.SwapHoriz, null) }, label = { Text("Mes échanges") })
            }
        },
    ) { pad ->
        Box(Modifier.padding(pad)) {
            if (onglet == 0) CatalogueTab(repo) else EchangesTab(repo, onSeance)
        }
    }
}

@Composable
private fun CatalogueTab(repo: Repository) {
    var motCle by remember { mutableStateOf("") }
    var liste by remember { mutableStateOf<List<Competence>>(emptyList()) }
    var erreur by remember { mutableStateOf<String?>(null) }
    var charge by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun charger() {
        charge = true; erreur = null
        scope.launch {
            try { liste = repo.api.competences(motCle.ifBlank { null }) }
            catch (e: Exception) { erreur = e.messageUtilisateur() }
            charge = false
        }
    }
    LaunchedEffect(Unit) { charger() }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(motCle, { motCle = it }, singleLine = true, label = { Text("Rechercher une compétence") },
            modifier = Modifier.fillMaxWidth(), trailingIcon = { TextButton(onClick = { charger() }) { Text("OK") } })
        MessageErreur(erreur)
        message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        if (charge) LinearProgressIndicator(Modifier.fillMaxWidth())
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            items(liste, key = { it.id }) { c ->
                Carte {
                    Text(c.titre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        c.categorie?.let { Etiquette(it.nom) }
                        if (c.niveau.isNotBlank()) Etiquette(c.niveau.lowercase().replaceFirstChar { it.uppercase() })
                    }
                    Text(c.description, maxLines = 3, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (c.etudiantId != repo.utilisateur?.id) {
                        Button(onClick = {
                            scope.launch {
                                try {
                                    // Demande rapide : 1 h, dans 3 jours à 18h (le formulaire détaillé est sur le web)
                                    val date = java.time.LocalDate.now().plusDays(3).atTime(18, 0)
                                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                                    repo.api.creerEchange(ExchangeRequest(c.id, "Demande depuis l'application mobile", 1, date))
                                    message = "Demande envoyée pour « ${c.titre} » (voir Mes échanges)."
                                    erreur = null
                                } catch (e: Exception) { erreur = e.messageUtilisateur() }
                            }
                        }) { Text("Demander un échange") }
                    }
                }
            }
        }
    }
}

@Composable
private fun EchangesTab(repo: Repository, onSeance: (Long) -> Unit) {
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
    LaunchedEffect(Unit) { charger() }

    fun action(appel: suspend () -> Exchange) = scope.launch {
        try { appel(); charger() } catch (e: Exception) {
            erreur = e.messageUtilisateur()
            if (e is HttpException && e.code() == 409) charger()
        }
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
                        FilledTonalButton(onClick = { onSeance(e.id) }, modifier = Modifier.fillMaxWidth()) { Text("Fiche de la séance") }
                }
            }
        }
    }
}

package tn.skillswap.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import tn.skillswap.app.data.Exchange
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tn.skillswap.app.data.Repository
import tn.skillswap.app.ui.vmDe
import tn.skillswap.app.vm.AlertesViewModel

class HomeActions(
    val ouvrirCompetence: (Long) -> Unit,
    val ouvrirSeance: (Long) -> Unit,
    val donnerAvis: (Exchange) -> Unit,
    val ajouterAuCalendrier: (Exchange) -> Unit,
    val ouvrirAssistant: () -> Unit,
    val ouvrirLien: (String) -> Unit,
    val ouvrirCredits: () -> Unit,
    val ouvrirMesCompetences: () -> Unit,
    val deconnexion: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(repo: Repository, actions: HomeActions, refresh: Int) {
    var onglet by rememberSaveable { mutableIntStateOf(0) }
    // Le ViewModel est partagé entre le badge de la barre du bas et l'onglet Alertes.
    val alertesVm = vmDe { AlertesViewModel(repo.api) }
    val alertes by alertesVm.etat.collectAsStateWithLifecycle()
    val nonLues = alertes.nonLues

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("SkillSwap", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = actions.ouvrirAssistant) { Icon(Icons.Filled.AutoAwesome, contentDescription = "Assistant IA") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(onglet == 0, { onglet = 0 }, { Icon(Icons.Filled.School, contentDescription = null) }, label = { Text("Catalogue") })
                NavigationBarItem(onglet == 1, { onglet = 1 }, { Icon(Icons.Filled.SwapHoriz, null) }, label = { Text("Échanges") })
                NavigationBarItem(onglet == 2, { onglet = 2 },
                    { BadgedBox(badge = { if (nonLues > 0) Badge { Text(nonLues.toString()) } }) { Icon(Icons.Filled.Notifications, null) } },
                    label = { Text("Alertes") })
                NavigationBarItem(onglet == 3, { onglet = 3 }, { Icon(Icons.Filled.Person, null) }, label = { Text("Profil") })
            }
        },
    ) { pad ->
        Box(Modifier.padding(pad)) {
            when (onglet) {
                0 -> CatalogueTab(repo, actions.ouvrirCompetence, refresh)
                1 -> EchangesTab(repo, actions, refresh)
                2 -> AlertesTab(alertesVm, actions.ouvrirSeance, refresh)
                else -> ProfilTab(repo, actions, refresh)
            }
        }
    }
    LaunchedEffect(refresh) { if (refresh > 0) alertesVm.charger() }
}

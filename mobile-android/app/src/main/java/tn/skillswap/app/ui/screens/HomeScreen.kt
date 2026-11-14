package tn.skillswap.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import tn.skillswap.app.data.Exchange
import tn.skillswap.app.data.Repository

/** Actions déclenchées par les écrans : chacune démarre une autre Activity (Intent explicite) dans HomeActivity. */
class HomeActions(
    val ouvrirCompetence: (Long) -> Unit,
    val ouvrirSeance: (Long) -> Unit,
    val donnerAvis: (Exchange) -> Unit,
    val ajouterAuCalendrier: (Exchange) -> Unit,
    val ouvrirAssistant: () -> Unit,
    val deconnexion: () -> Unit,
)

/** Accueil : 4 onglets (Catalogue, Échanges, Alertes, Profil) + bouton de l'assistant IA. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(repo: Repository, actions: HomeActions, refresh: Int) {
    var onglet by remember { mutableIntStateOf(0) }
    var nonLues by remember { mutableIntStateOf(0) }

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
                NavigationBarItem(onglet == 0, { onglet = 0 }, { Icon(Icons.Filled.School, null) }, label = { Text("Catalogue") })
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
                2 -> AlertesTab(repo, actions.ouvrirSeance, refresh) { nonLues = it }
                else -> ProfilTab(repo, actions.deconnexion, refresh)
            }
        }
    }
    // Le compteur de la pastille est aussi chargé au démarrage (sans ouvrir l'onglet Alertes)
    LaunchedEffect(refresh) { try { nonLues = repo.api.notifications().unreadCount } catch (_: Exception) {} }
}

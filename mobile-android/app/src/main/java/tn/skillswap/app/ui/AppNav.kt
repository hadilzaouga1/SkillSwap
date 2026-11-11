package tn.skillswap.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import tn.skillswap.app.data.Repository
import tn.skillswap.app.ui.screens.*

/** Navigation : connexion -> accueil (catalogue / échanges) -> fiche de séance. */
@Composable
fun AppNav(repo: Repository) {
    val nav = rememberNavController()
    var depart by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { depart = if (repo.restaurerSession()) "accueil" else "connexion" }

    val start = depart
    if (start == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    NavHost(nav, startDestination = start) {
        composable("connexion") {
            LoginScreen(repo) { nav.navigate("accueil") { popUpTo("connexion") { inclusive = true } } }
        }
        composable("accueil") {
            HomeScreen(
                repo,
                onSeance = { id -> nav.navigate("seance/$id") },
                onDeconnexion = { nav.navigate("connexion") { popUpTo("accueil") { inclusive = true } } },
            )
        }
        composable("seance/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { entry ->
            SeanceScreen(repo, entry.arguments!!.getLong("id")) { nav.popBackStack() }
        }
    }
}

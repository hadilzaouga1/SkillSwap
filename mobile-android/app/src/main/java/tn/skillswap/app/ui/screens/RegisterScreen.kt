package tn.skillswap.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import tn.skillswap.app.data.Repository
import tn.skillswap.app.ui.ouvrirLien

/**
 * Les comptes sont gérés par Keycloak : l'inscription se fait sur la page Keycloak du navigateur
 * (Intent implicite ACTION_VIEW), puis l'étudiant revient se connecter dans l'application.
 */
@Composable
fun RegisterScreen(repo: Repository, onRetour: () -> Unit) {
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Créer un compte", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary)
        Carte {
            Text("L'inscription se fait sur la page sécurisée de SkillSwap (Keycloak), dans votre navigateur.")
            Text("1. Appuyez sur « Ouvrir la page d'inscription » et remplissez le formulaire (email, prénom, nom, mot de passe).",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("2. Revenez dans l'application et connectez-vous avec cet email et ce mot de passe.",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = { ctx.ouvrirLien(repo.urlInscription()) }, modifier = Modifier.fillMaxWidth()) {
                Text("Ouvrir la page d'inscription")
            }
            TextButton(onClick = onRetour, modifier = Modifier.fillMaxWidth()) { Text("Retour à la connexion") }
        }
    }
}

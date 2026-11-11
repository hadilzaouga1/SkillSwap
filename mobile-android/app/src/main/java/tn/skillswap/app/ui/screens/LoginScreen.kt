package tn.skillswap.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tn.skillswap.app.data.Repository
import tn.skillswap.app.data.messageUtilisateur

@Composable
fun LoginScreen(repo: Repository, onConnecte: () -> Unit) {
    var email by remember { mutableStateOf("hadil@univ.tn") }  // compte de démonstration
    var mdp by remember { mutableStateOf("Password123!") }
    var erreur by remember { mutableStateOf<String?>(null) }
    var charge by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("SkillSwap", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary)
        Text("Échangez vos compétences : 1 h = 1 crédit", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        Carte {
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(mdp, { mdp = it }, label = { Text("Mot de passe") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            MessageErreur(erreur)
            Button(
                onClick = {
                    charge = true; erreur = null
                    scope.launch {
                        try { repo.connexion(email, mdp); onConnecte() }
                        catch (e: Exception) { erreur = e.messageUtilisateur() }
                        charge = false
                    }
                },
                enabled = !charge && email.isNotBlank() && mdp.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (charge) "Connexion…" else "Se connecter") }
        }
    }
}

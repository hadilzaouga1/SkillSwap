package tn.skillswap.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import tn.skillswap.app.data.*

@Composable
fun RegisterScreen(repo: Repository, onTermine: () -> Unit, onRetour: () -> Unit) {
    var nom by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mdp by remember { mutableStateOf("") }
    var univ by remember { mutableStateOf("") }
    var filiere by remember { mutableStateOf("") }
    var erreur by remember { mutableStateOf<String?>(null) }
    var charge by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val valide = nom.isNotBlank() && email.contains('@') && mdp.length >= 8 && univ.isNotBlank() && filiere.isNotBlank()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Créer un compte", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary)
        Carte {
            OutlinedTextField(nom, { nom = it }, label = { Text("Nom complet") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(mdp, { mdp = it }, label = { Text("Mot de passe (8 caractères min.)") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(univ, { univ = it }, label = { Text("Université") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(filiere, { filiere = it }, label = { Text("Filière") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            MessageErreur(erreur)
            Button(enabled = valide && !charge, modifier = Modifier.fillMaxWidth(), onClick = {
                charge = true; erreur = null
                scope.launch {
                    try { repo.inscription(InscriptionRequest(nom.trim(), email.trim(), mdp, univ.trim(), filiere.trim())); onTermine() }
                    catch (e: Exception) { erreur = e.messageUtilisateur() }
                    charge = false
                }
            }) { Text(if (charge) "Création…" else "S'inscrire") }
            TextButton(onClick = onRetour, modifier = Modifier.fillMaxWidth()) { Text("Retour à la connexion") }
        }
    }
}

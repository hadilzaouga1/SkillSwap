package tn.skillswap.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import tn.skillswap.app.ui.screens.LoginScreen
import tn.skillswap.app.ui.theme.SkillSwapTheme

/**
 * ACTIVITY 1 — point d'entrée (LAUNCHER). Si un jeton est déjà enregistré, on passe directement à l'accueil.
 * Navigation par INTENT EXPLICITE : startActivity(Intent(this, HomeActivity::class.java)).
 */
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkillSwapTheme {
                var pret by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { if (repo.restaurerSession()) allerAccueil() else pret = true }
                if (pret) {
                    LoginScreen(repo, onConnecte = { allerAccueil() },
                        onInscription = { startActivity(Intent(this@LoginActivity, RegisterActivity::class.java)) })
                } else Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
        }
    }

    private fun allerAccueil() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish() // on retire l'écran de connexion de la pile : "Retour" ne revient pas dessus
    }
}

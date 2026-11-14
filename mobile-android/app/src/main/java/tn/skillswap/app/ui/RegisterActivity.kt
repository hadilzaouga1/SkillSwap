package tn.skillswap.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import tn.skillswap.app.ui.screens.RegisterScreen
import tn.skillswap.app.ui.theme.SkillSwapTheme

/** ACTIVITY 2 — création de compte. Après l'inscription, on repart d'une pile vide (FLAG_ACTIVITY_CLEAR_TASK). */
class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkillSwapTheme {
                RegisterScreen(repo,
                    onTermine = {
                        startActivity(Intent(this, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                    },
                    onRetour = { finish() })
            }
        }
    }
}

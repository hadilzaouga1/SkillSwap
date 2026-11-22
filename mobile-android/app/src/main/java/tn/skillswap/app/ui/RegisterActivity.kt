package tn.skillswap.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import tn.skillswap.app.ui.screens.RegisterScreen
import tn.skillswap.app.ui.theme.SkillSwapTheme

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkillSwapTheme {
                RegisterScreen(repo, onRetour = { finish() })
            }
        }
    }
}

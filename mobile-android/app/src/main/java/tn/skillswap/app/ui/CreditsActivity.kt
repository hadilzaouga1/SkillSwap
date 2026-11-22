package tn.skillswap.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import tn.skillswap.app.ui.screens.CreditsScreen
import tn.skillswap.app.ui.theme.SkillSwapTheme
import tn.skillswap.app.vm.CreditsViewModel

/** Solde et historique des crédits (credit-service). */
class CreditsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkillSwapTheme {
                val vm = vmDe { CreditsViewModel(repo.api) }
                CreditsScreen(vm, onRetour = { finish() })
            }
        }
    }
}

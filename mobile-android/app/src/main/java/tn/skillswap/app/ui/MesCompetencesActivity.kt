package tn.skillswap.app.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import tn.skillswap.app.ui.screens.MesCompetencesScreen
import tn.skillswap.app.ui.theme.SkillSwapTheme
import tn.skillswap.app.vm.MesCompetencesViewModel

/** Publier, modifier et supprimer mes compétences (skill-service). */
class MesCompetencesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_OK)   // le catalogue se rafraîchit au retour
        setContent {
            SkillSwapTheme {
                val vm = vmDe { MesCompetencesViewModel(repo.api) }
                MesCompetencesScreen(vm, onRetour = { finish() })
            }
        }
    }
}

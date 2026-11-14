package tn.skillswap.app.ui

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import tn.skillswap.app.ui.screens.SkillDetailScreen
import tn.skillswap.app.ui.theme.SkillSwapTheme

/** ACTIVITY 4 — détail d'une compétence et demande d'échange. Reçoit SKILL_ID dans l'Intent et renvoie RESULT_OK. */
class SkillDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val skillId = intent.getLongExtra(Extras.SKILL_ID, -1L)   // lecture d'un extra de l'Intent
        if (skillId < 0) { finish(); return }
        setContent {
            SkillSwapTheme {
                SkillDetailScreen(repo, skillId, onRetour = { finish() }, onEnvoye = {
                    Toast.makeText(this, "Demande envoyée !", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)   // prévient HomeActivity de recharger ses listes
                    finish()
                })
            }
        }
    }
}

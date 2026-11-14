package tn.skillswap.app.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import tn.skillswap.app.ui.screens.SeanceScreen
import tn.skillswap.app.ui.theme.SkillSwapTheme

/** ACTIVITY 5 — fiche complète d'une séance (description, programme, PDF, liens). Reçoit EXCHANGE_ID. */
class SeanceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getLongExtra(Extras.EXCHANGE_ID, -1L)
        if (id < 0) { finish(); return }
        setContent { SkillSwapTheme { SeanceScreen(repo, id) { setResult(Activity.RESULT_OK); finish() } } }
    }
}

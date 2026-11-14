package tn.skillswap.app.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import tn.skillswap.app.ui.screens.AvisScreen
import tn.skillswap.app.ui.theme.SkillSwapTheme

class AvisActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getLongExtra(Extras.EXCHANGE_ID, -1L)
        val titre = intent.getStringExtra(Extras.EXCHANGE_TITLE).orEmpty()
        if (id < 0) { finish(); return }
        setContent {
            SkillSwapTheme {
                AvisScreen(repo, id, titre, onRetour = { finish() }, onPublie = { setResult(Activity.RESULT_OK); finish() })
            }
        }
    }
}

package tn.skillswap.app.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import tn.skillswap.app.ui.screens.AssistantScreen
import tn.skillswap.app.ui.theme.SkillSwapTheme

class AssistantActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val page = intent.getStringExtra(Extras.PAGE) ?: "partage"
        val partage = if (intent.action == Intent.ACTION_SEND) intent.getStringExtra(Intent.EXTRA_TEXT) else null
        setContent {
            SkillSwapTheme {
                AssistantScreen(repo, page, partage, onRetour = { finish() },
                    onCompetence = { id -> startActivity(intentVers<SkillDetailActivity>(Extras.SKILL_ID to id)) })
            }
        }
    }
}

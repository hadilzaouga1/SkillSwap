package tn.skillswap.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import tn.skillswap.app.ui.AppNav
import tn.skillswap.app.ui.theme.SkillSwapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = (application as SkillSwapApp).repository
        setContent {
            SkillSwapTheme { AppNav(repo) }
        }
    }
}

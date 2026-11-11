package tn.skillswap.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Même palette que le site web : vert sauge calme, fond doux.
val Sauge = Color(0xFF5B8A72)
val SaugeFonce = Color(0xFF4A725D)
val Fond = Color(0xFFF4F7F6)
val Texte = Color(0xFF2E3A46)

private val Couleurs = lightColorScheme(
    primary = Sauge, onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEBE3), onPrimaryContainer = SaugeFonce,
    background = Fond, onBackground = Texte,
    surface = Color.White, onSurface = Texte,
    surfaceVariant = Color(0xFFEEF2F0), onSurfaceVariant = Color(0xFF6B7782),
)

@Composable
fun SkillSwapTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Couleurs, content = content)
}

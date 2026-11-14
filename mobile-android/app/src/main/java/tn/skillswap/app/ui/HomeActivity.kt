package tn.skillswap.app.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableIntStateOf
import tn.skillswap.app.data.Exchange
import tn.skillswap.app.ui.screens.HomeActions
import tn.skillswap.app.ui.screens.HomeScreen
import tn.skillswap.app.ui.theme.SkillSwapTheme
import java.time.LocalDateTime
import java.time.ZoneId

class HomeActivity : ComponentActivity() {

    private val refresh = mutableIntStateOf(0)

    private val retourEnfant = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        refresh.intValue++
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val actions = HomeActions(
            ouvrirCompetence = { id -> retourEnfant.launch(intentVers<SkillDetailActivity>(Extras.SKILL_ID to id)) },
            ouvrirSeance = { id -> retourEnfant.launch(intentVers<SeanceActivity>(Extras.EXCHANGE_ID to id)) },
            donnerAvis = { e -> retourEnfant.launch(intentVers<AvisActivity>(Extras.EXCHANGE_ID to e.id, Extras.EXCHANGE_TITLE to e.skillTitle)) },
            ouvrirAssistant = { startActivity(intentVers<AssistantActivity>(Extras.PAGE to "accueil")) },
            ajouterAuCalendrier = ::ajouterAuCalendrier,
            deconnexion = {
                startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            },
        )
        setContent { SkillSwapTheme { HomeScreen(repo, actions, refresh.intValue) } }
    }

    private fun ajouterAuCalendrier(e: Exchange) {
        try {
            val debut = LocalDateTime.parse(e.proposedDateTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val intent = Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, "SkillSwap : ${e.skillTitle}")
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, debut)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, debut + e.durationHours * 3_600_000L)
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "Aucune application Calendrier trouvée.", Toast.LENGTH_SHORT).show()
        } catch (_: Exception) {
            Toast.makeText(this, "Date de séance invalide.", Toast.LENGTH_SHORT).show()
        }
    }
}

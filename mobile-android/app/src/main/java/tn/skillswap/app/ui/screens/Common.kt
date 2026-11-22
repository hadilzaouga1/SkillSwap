package tn.skillswap.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Carte(modifier: Modifier = Modifier, contenu: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = contenu) }
}

@Composable
fun Etiquette(texte: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Text(texte, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@Composable
fun MessageErreur(texte: String?) {
    if (texte != null) Text(texte, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
}

val STATUTS = mapOf(
    "PENDING" to "En attente", "TIME_PROPOSED" to "Nouveau créneau proposé", "ACCEPTED" to "Acceptée",
    "AWAITING_CONFIRMATION" to "À confirmer", "COMPLETED" to "Terminée", "REJECTED" to "Refusée", "CANCELLED" to "Annulée",
)
val MODES = mapOf("ONLINE" to "En ligne", "IN_PERSON" to "En présentiel", "HYBRID" to "Hybride")
val FORMATS = mapOf("COURS" to "Cours", "ATELIER" to "Atelier", "TUTORAT" to "Tutorat", "REVISION" to "Révision", "PROJET" to "Projet")

val STATUTS_COMPETENCE = mapOf("EN_ATTENTE" to "En attente de validation", "VALIDEE" to "Validée", "REFUSEE" to "Refusée")

fun libelleNiveau(niveau: String): String = when (niveau.uppercase()) {
    "DEBUTANT" -> "Débutant"
    "INTERMEDIAIRE" -> "Intermédiaire"
    "AVANCE" -> "Avancé"
    else -> niveau.lowercase().replaceFirstChar { it.uppercase() }
}

/** Bandeau affiché quand les données viennent du cache Room (serveur injoignable). */
@Composable
fun BandeauHorsLigne(visible: Boolean) {
    androidx.compose.animation.AnimatedVisibility(visible) {
        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Text("Mode hors ligne : données enregistrées sur le téléphone.", Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** Date puis heure avec les boîtes de dialogue natives ; [onChoix] reçoit un créneau futur uniquement. */
fun choisirDateHeure(ctx: android.content.Context, onChoix: (java.time.LocalDateTime) -> Unit, onPasse: () -> Unit = {}) {
    val base = java.time.LocalDateTime.now().plusDays(1).withMinute(0)
    android.app.DatePickerDialog(ctx, { _, a, m, j ->
        android.app.TimePickerDialog(ctx, { _, h, min ->
            val choisi = java.time.LocalDateTime.of(a, m + 1, j, h, min)
            if (choisi.isAfter(java.time.LocalDateTime.now())) onChoix(choisi) else onPasse()
        }, base.hour, 0, true).show()
    }, base.year, base.monthValue - 1, base.dayOfMonth).show()
}

fun java.time.LocalDateTime.versIso(): String = format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))

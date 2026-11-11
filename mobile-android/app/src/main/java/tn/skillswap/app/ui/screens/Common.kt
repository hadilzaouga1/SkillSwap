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

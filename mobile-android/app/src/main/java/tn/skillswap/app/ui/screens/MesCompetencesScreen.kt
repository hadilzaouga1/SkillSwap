package tn.skillswap.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tn.skillswap.app.data.Categorie
import tn.skillswap.app.data.Competence
import tn.skillswap.app.vm.MesCompetencesViewModel
import tn.skillswap.app.vm.NIVEAUX

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MesCompetencesScreen(vm: MesCompetencesViewModel, onRetour: () -> Unit) {
    val etat by vm.etat.collectAsStateWithLifecycle()
    var formulaire by rememberSaveable { mutableStateOf(false) }
    var edition by remember { mutableStateOf<Competence?>(null) }
    var aSupprimer by remember { mutableStateOf<Competence?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mes compétences") },
                navigationIcon = { IconButton(onClick = onRetour) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour") } })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { edition = null; formulaire = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) }, text = { Text("Publier") })
        },
    ) { pad ->
        LazyColumn(Modifier.padding(pad).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp)) {
            item {
                if (etat.enChargement) LinearProgressIndicator(Modifier.fillMaxWidth())
                if (!formulaire) MessageErreur(etat.erreur)
                Text("Une nouvelle compétence est visible dans le catalogue après validation par un modérateur.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!etat.enChargement && etat.liste.isEmpty() && etat.erreur == null)
                    Text("Vous n'avez publié aucune compétence.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(etat.liste, key = { it.id }) { c ->
                Carte {
                    Text(c.titre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Etiquette(STATUTS_COMPETENCE[c.statutValidation] ?: c.statutValidation)
                        c.categorie?.let { Etiquette(it.nom) }
                        if (c.niveau.isNotBlank()) Etiquette(libelleNiveau(c.niveau))
                    }
                    if (c.description.isNotBlank()) Text(c.description, maxLines = 3, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row {
                        IconButton(onClick = { edition = c; formulaire = true }) { Icon(Icons.Filled.Edit, contentDescription = "Modifier ${c.titre}") }
                        IconButton(onClick = { aSupprimer = c }) { Icon(Icons.Filled.Delete, contentDescription = "Supprimer ${c.titre}") }
                    }
                }
            }
        }
    }

    if (formulaire) FormulaireCompetence(
        initial = edition, categories = etat.categories, enregistrement = etat.enregistrement, erreur = etat.erreur,
        onAnnuler = { formulaire = false },
        onValider = { titre, desc, niveau, cat, dispos -> vm.enregistrer(edition?.id, titre, desc, niveau, cat, dispos) { formulaire = false } },
    )
    aSupprimer?.let { c ->
        AlertDialog(
            onDismissRequest = { aSupprimer = null },
            title = { Text("Supprimer cette compétence ?") },
            text = { Text("« ${c.titre} » ne sera plus visible dans le catalogue.") },
            confirmButton = { TextButton(onClick = { vm.supprimer(c.id); aSupprimer = null }) { Text("Supprimer") } },
            dismissButton = { TextButton(onClick = { aSupprimer = null }) { Text("Annuler") } },
        )
    }
}

@Composable
private fun FormulaireCompetence(
    initial: Competence?, categories: List<Categorie>, enregistrement: Boolean, erreur: String?,
    onAnnuler: () -> Unit, onValider: (String, String, String?, Long?, String) -> Unit,
) {
    var titre by rememberSaveable { mutableStateOf(initial?.titre.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(initial?.description.orEmpty()) }
    var niveau by rememberSaveable { mutableStateOf(initial?.niveau?.takeIf { it.isNotBlank() }) }
    var categorieId by rememberSaveable { mutableStateOf(initial?.categorie?.id) }
    var dispos by rememberSaveable { mutableStateOf(initial?.disponibilites.orEmpty()) }

    AlertDialog(
        onDismissRequest = { if (!enregistrement) onAnnuler() },
        title = { Text(if (initial == null) "Publier une compétence" else "Modifier la compétence") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(titre, { titre = it.take(120) }, label = { Text("Titre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(description, { description = it.take(2000) }, label = { Text("Description") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                Text("Niveau", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(NIVEAUX, key = { it.first }) { (code, nom) -> FilterChip(niveau == code, { niveau = code }, label = { Text(nom) }) }
                }
                Text("Catégorie", style = MaterialTheme.typography.labelLarge)
                if (categories.isEmpty()) Text("Chargement des catégories…", style = MaterialTheme.typography.bodySmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories, key = { it.id }) { c -> FilterChip(categorieId == c.id, { categorieId = c.id }, label = { Text(c.nom) }) }
                }
                OutlinedTextField(dispos, { dispos = it.take(255) }, label = { Text("Disponibilités (ex. lundi soir)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                MessageErreur(erreur)
            }
        },
        confirmButton = {
            Button(enabled = !enregistrement, onClick = { onValider(titre, description, niveau, categorieId, dispos) }) {
                Text(if (enregistrement) "Enregistrement…" else "Enregistrer")
            }
        },
        dismissButton = { TextButton(enabled = !enregistrement, onClick = onAnnuler) { Text("Annuler") } },
    )
}

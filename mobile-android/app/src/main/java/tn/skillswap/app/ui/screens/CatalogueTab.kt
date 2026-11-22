package tn.skillswap.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tn.skillswap.app.data.Competence
import tn.skillswap.app.data.Repository
import tn.skillswap.app.ui.vmDe
import tn.skillswap.app.vm.CatalogueViewModel
import tn.skillswap.app.vm.NIVEAUX

@Composable
fun CatalogueTab(repo: Repository, onCompetence: (Long) -> Unit, refresh: Int) {
    val vm = vmDe { CatalogueViewModel(repo.api, repo.cache) }
    val etat by vm.etat.collectAsStateWithLifecycle()
    val liste by vm.competences.collectAsStateWithLifecycle()
    LaunchedEffect(refresh) { if (refresh > 0) vm.rafraichir() }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(etat.motCle, vm::changerMotCle, singleLine = true, label = { Text("Rechercher une compétence") },
                modifier = Modifier.weight(1f))
            IconButton(onClick = vm::basculerGrille) {
                Icon(if (etat.grille) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.GridView,
                    contentDescription = if (etat.grille) "Afficher en liste" else "Afficher en grille")
            }
        }
        // Filtres : niveau puis catégorie
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(etat.niveau == null, { vm.choisirNiveau(null) }, label = { Text("Tous niveaux") }) }
            items(NIVEAUX, key = { it.first }) { (code, nom) ->
                FilterChip(etat.niveau == code, { vm.choisirNiveau(if (etat.niveau == code) null else code) }, label = { Text(nom) })
            }
        }
        if (etat.categories.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(etat.categorieId == null, { vm.choisirCategorie(null) }, label = { Text("Toutes catégories") }) }
                items(etat.categories, key = { it.id }) { c ->
                    FilterChip(etat.categorieId == c.id, { vm.choisirCategorie(if (etat.categorieId == c.id) null else c.id) }, label = { Text(c.nom) })
                }
            }
        }
        BandeauHorsLigne(etat.horsLigne)
        if (etat.erreur != null && !etat.horsLigne) MessageErreur(etat.erreur)
        if (etat.enChargement) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (!etat.enChargement && liste.isEmpty()) Text("Aucune compétence trouvée.", color = MaterialTheme.colorScheme.onSurfaceVariant)

        if (etat.grille) {
            LazyVerticalGrid(GridCells.Adaptive(170.dp), horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(liste, key = { it.id }) { c -> CarteCompetence(c, onCompetence) }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
                items(liste, key = { it.id }) { c -> CarteCompetence(c, onCompetence) }
            }
        }
    }
}

@Composable
fun CarteCompetence(c: Competence, onClick: (Long) -> Unit) {
    Carte(Modifier.animateContentSize().clickable { onClick(c.id) }.semantics { contentDescription = "Compétence ${c.titre}" }) {
        Text(c.titre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            c.categorie?.let { Etiquette(it.nom) }
            if (c.niveau.isNotBlank()) Etiquette(libelleNiveau(c.niveau))
        }
        Text(c.description, maxLines = 3, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Voir le détail →", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    }
}

package tn.skillswap.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import tn.skillswap.app.data.Repository
import tn.skillswap.app.ui.vmDe
import tn.skillswap.app.vm.ProfilViewModel

@Composable
fun ProfilTab(repo: Repository, actions: HomeActions, refresh: Int) {
    val moi = repo.utilisateur
    val vm = vmDe { ProfilViewModel(repo.api, moi?.id) }
    val etat by vm.etat.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    LaunchedEffect(refresh) { if (refresh > 0) vm.charger() }

    // Champs du formulaire : rememberSaveable pour survivre à la rotation de l'écran
    var nom by rememberSaveable { mutableStateOf("") }
    var universite by rememberSaveable { mutableStateOf("") }
    var filiere by rememberSaveable { mutableStateOf("") }
    var initialise by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(etat.profil) {
        val p = etat.profil
        if (p != null && !initialise) { nom = p.nom; universite = p.universite.orEmpty(); filiere = p.filiere.orEmpty(); initialise = true }
    }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)) {
        item {
            Carte {
                Text(etat.profil?.nom ?: moi?.nom.orEmpty(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Etiquette(moi?.role.orEmpty())
                val r = etat.reputation
                if (r == null || r.count == 0) Text("Pas encore d'avis reçus.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                else {
                    Text("★ ${"%.1f".format(r.average)} / 5  (${r.count} avis)", style = MaterialTheme.typography.titleMedium)
                    r.badge?.let { Etiquette("🏅 ${it.label}") }
                }
            }
        }
        item {
            Carte {
                Text("Mes crédits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                val s = etat.solde
                if (s == null) Text("Solde indisponible.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                else {
                    Text("${s.available} h disponibles", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary)
                    if (s.reserved > 0) Text("${s.reserved} h bloquées dans des échanges en cours", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                OutlinedButton(onClick = actions.ouvrirCredits, modifier = Modifier.fillMaxWidth()) { Text("Historique des crédits") }
            }
        }
        item {
            Carte {
                Text("Mes compétences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Publiez, modifiez ou supprimez les compétences que vous proposez.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = actions.ouvrirMesCompetences, modifier = Modifier.fillMaxWidth()) { Text("Gérer mes compétences") }
            }
        }
        item {
            Carte {
                Text("Modifier mon profil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(nom, { nom = it }, label = { Text("Nom") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(universite, { universite = it }, label = { Text("Université") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(filiere, { filiere = it }, label = { Text("Filière") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                MessageErreur(etat.erreur)
                etat.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                Button(enabled = !etat.enregistrement, onClick = { vm.enregistrer(nom, universite, filiere) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (etat.enregistrement) "Enregistrement…" else "Enregistrer")
                }
                OutlinedButton(onClick = { scope.launch { repo.deconnexion(); actions.deconnexion() } }, modifier = Modifier.fillMaxWidth()) {
                    Text("Se déconnecter")
                }
            }
        }
        item { Text("Avis reçus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
        items(etat.reputation?.reviews.orEmpty(), key = { it.id }) { a ->
            Carte {
                Text("★".repeat(a.rating) + "☆".repeat(5 - a.rating))
                a.comment?.let { Text(it) }
                a.skillTitle?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

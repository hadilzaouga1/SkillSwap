package tn.skillswap.app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import tn.skillswap.app.data.CategorieInfo
import tn.skillswap.app.data.Competence
import tn.skillswap.app.data.Exchange
import tn.skillswap.app.data.local.CacheStore

/** Cache en mémoire : remplace Room dans les tests unitaires. */
class FakeCacheStore : CacheStore {
    val competencesFlow = MutableStateFlow<List<Competence>>(emptyList())
    private val echangesFlow = MutableStateFlow<Map<String, List<Exchange>>>(emptyMap())

    override fun competences(): Flow<List<Competence>> = competencesFlow
    override suspend fun remplacerCompetences(liste: List<Competence>) { competencesFlow.value = liste }
    override fun echanges(origine: String): Flow<List<Exchange>> = echangesFlow.map { it[origine].orEmpty() }
    override suspend fun remplacerEchanges(origine: String, liste: List<Exchange>) {
        echangesFlow.value = echangesFlow.value + (origine to liste)
    }
}

fun competence(id: Long, titre: String, niveau: String = "DEBUTANT", categorie: Long = 1, description: String = "") =
    Competence(id = id, titre = titre, description = description, niveau = niveau, categorie = CategorieInfo(categorie, "Cat $categorie"))

fun echange(id: Long, statut: String = "PENDING") =
    Exchange(id = id, requesterId = 1, providerId = 2, skillTitle = "Cours $id", status = statut)

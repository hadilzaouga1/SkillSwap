package tn.skillswap.app

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import tn.skillswap.app.data.Api
import tn.skillswap.app.data.Categorie
import tn.skillswap.app.vm.CatalogueViewModel
import tn.skillswap.app.vm.filtrerCompetences
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogueViewModelTest {
    private val api = mockk<Api>()
    private val cache = FakeCacheStore()

    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private val liste = listOf(
        competence(1, "Cours d'Angular", "AVANCE", 1, "Framework web"),
        competence(2, "Cours Python", "INTERMEDIAIRE", 1),
        competence(3, "Révision TCF", "AVANCE", 2, "Français"),
    )

    @Test fun `le rafraichissement remplit le cache et les categories`() {
        coEvery { api.competences(any(), any(), any()) } returns liste
        coEvery { api.categories() } returns listOf(Categorie(1, "Informatique"), Categorie(2, "Langues"))
        val vm = CatalogueViewModel(api, cache)
        assertEquals(3, vm.competences.value.size)
        assertEquals(2, vm.etat.value.categories.size)
        assertFalse(vm.etat.value.horsLigne)
        assertFalse(vm.etat.value.enChargement)
    }

    @Test fun `hors ligne - le cache reste affiche et le bandeau s'active`() {
        cache.competencesFlow.value = liste
        coEvery { api.competences(any(), any(), any()) } throws IOException("réseau")
        coEvery { api.categories() } throws IOException("réseau")
        val vm = CatalogueViewModel(api, cache)
        assertEquals(3, vm.competences.value.size)
        assertTrue(vm.etat.value.horsLigne)
        assertTrue(vm.etat.value.erreur!!.contains("joindre"))
    }

    @Test fun `les filtres combinent mot-cle, niveau et categorie`() {
        coEvery { api.competences(any(), any(), any()) } returns liste
        coEvery { api.categories() } returns emptyList()
        val vm = CatalogueViewModel(api, cache)
        vm.choisirNiveau("AVANCE")
        assertEquals(listOf(1L, 3L), vm.competences.value.map { it.id })
        vm.choisirCategorie(2)
        assertEquals(listOf(3L), vm.competences.value.map { it.id })
        vm.choisirCategorie(null); vm.choisirNiveau(null); vm.changerMotCle("python")
        assertEquals(listOf(2L), vm.competences.value.map { it.id })
    }

    @Test fun `basculer la grille inverse l'affichage`() {
        coEvery { api.competences(any(), any(), any()) } returns emptyList()
        coEvery { api.categories() } returns emptyList()
        val vm = CatalogueViewModel(api, cache)
        assertFalse(vm.etat.value.grille)
        vm.basculerGrille()
        assertTrue(vm.etat.value.grille)
    }

    @Test fun `filtrerCompetences cherche aussi dans la description et ignore la casse`() {
        assertEquals(listOf(1L), filtrerCompetences(liste, "FRAMEWORK", null, null).map { it.id })
        assertEquals(3, filtrerCompetences(liste, "  ", null, null).size)
        assertTrue(filtrerCompetences(liste, "zzz", null, null).isEmpty())
    }
}

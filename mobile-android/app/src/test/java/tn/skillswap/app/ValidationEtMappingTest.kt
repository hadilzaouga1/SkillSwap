package tn.skillswap.app

import io.mockk.coEvery
import io.mockk.coVerify
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
import tn.skillswap.app.data.*
import tn.skillswap.app.data.local.versEntite
import tn.skillswap.app.data.local.versModele
import tn.skillswap.app.vm.*

@OptIn(ExperimentalCoroutinesApi::class)
class ValidationEtMappingTest {
    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun `validerCompetence applique les regles du serveur`() {
        assertNotNull(validerCompetence("  ", "", 1, "AVANCE"))
        assertNotNull(validerCompetence("a".repeat(121), "", 1, "AVANCE"))
        assertNotNull(validerCompetence("Titre", "x".repeat(2001), 1, "AVANCE"))
        assertNotNull(validerCompetence("Titre", "", null, "AVANCE"))
        assertNotNull(validerCompetence("Titre", "", 1, null))
        assertNull(validerCompetence("Titre", "Description", 1, "AVANCE"))
    }

    @Test fun `validerProfil refuse un nom vide ou trop long`() {
        assertNotNull(validerProfil(" "))
        assertNotNull(validerProfil("n".repeat(81)))
        assertNull(validerProfil("Hadil"))
    }

    @Test fun `libelles de transactions`() {
        assertEquals("Crédits de bienvenue", libelleTransaction("WELCOME"))
        assertEquals("INCONNU", libelleTransaction("INCONNU"))
    }

    @Test fun `le mapping Room conserve tous les champs d'une competence`() {
        val c = Competence(7, "Titre", "Desc", "AVANCE", "Lundi", "VALIDEE", CategorieInfo(3, "Langues"), 9, "/api/competences/files/a.pdf", "a.pdf", 120)
        assertEquals(c, c.versEntite().versModele())
    }

    @Test fun `le mapping Room conserve les champs d'un echange et son origine`() {
        val e = Exchange(5, 1, 2, 3, "Cours", "msg", 2, "2027-01-01T10:00:00", "ACCEPTED", "https://meet.jit.si/x")
        val entite = e.versEntite("SENT")
        assertEquals("SENT", entite.origine)
        assertEquals(e, entite.versModele())
    }

    @Test fun `creer une competence valide appelle l'API et le callback`() {
        val api = mockk<Api>()
        coEvery { api.mesCompetences() } returns emptyList()
        coEvery { api.categories() } returns listOf(Categorie(1, "Informatique"))
        coEvery { api.creerCompetence(any()) } returns competence(1, "Titre")
        val vm = MesCompetencesViewModel(api)
        var ok = false
        vm.enregistrer(null, " Titre ", "Desc", "AVANCE", 1, "", onOk = { ok = true })
        coVerify { api.creerCompetence(CompetenceRequest("Titre", "Desc", "AVANCE", 1, null)) }
        assertTrue(ok)
        assertNull(vm.etat.value.erreur)
    }

    @Test fun `creer une competence invalide n'appelle pas l'API`() {
        val api = mockk<Api>()
        coEvery { api.mesCompetences() } returns emptyList()
        coEvery { api.categories() } returns emptyList()
        val vm = MesCompetencesViewModel(api)
        var ok = false
        vm.enregistrer(null, "", "", null, null, "", onOk = { ok = true })
        assertFalse(ok)
        assertNotNull(vm.etat.value.erreur)
        coVerify(exactly = 0) { api.creerCompetence(any()) }
    }

    @Test fun `le profil se charge puis s'enregistre`() {
        val api = mockk<Api>()
        coEvery { api.monProfil() } returns Profil(1, "Hadil", "h@u.tn")
        coEvery { api.solde() } returns Solde(1, 5, 1, 4)
        coEvery { api.reputation(1) } returns Reputation(1, 0.0, 0, emptyMap())
        coEvery { api.modifierProfil(any()) } returns Profil(1, "Hadil Z", "h@u.tn", "ISET", null)
        val vm = ProfilViewModel(api, 1)
        assertEquals(4, vm.etat.value.solde?.available)
        vm.enregistrer("Hadil Z", "ISET", " ")
        coVerify { api.modifierProfil(ProfilUpdate("Hadil Z", "ISET", null)) }
        assertEquals("Profil enregistré.", vm.etat.value.message)
    }

    @Test fun `les credits chargent le solde et l'historique`() {
        val api = mockk<Api>()
        coEvery { api.solde() } returns Solde(1, 3, 0, 3)
        coEvery { api.transactions() } returns listOf(TransactionCredit(1, "WELCOME", 3))
        val vm = CreditsViewModel(api)
        assertEquals(3, vm.etat.value.solde?.balance)
        assertEquals(1, vm.etat.value.transactions.size)
    }
}

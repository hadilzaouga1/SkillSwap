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
import tn.skillswap.app.data.Api
import tn.skillswap.app.data.ProposeTimeRequest
import tn.skillswap.app.vm.EchangesViewModel
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class EchangesViewModelTest {
    private val api = mockk<Api>()
    private val cache = FakeCacheStore()
    private val signales = mutableListOf<Long>()

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { api.recues() } returns listOf(echange(10), echange(11, "ACCEPTED"))
        coEvery { api.envoyees() } returns listOf(echange(20, "TIME_PROPOSED"))
        coEvery { api.resumes(any()) } returns emptyMap()
    }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun vm() = EchangesViewModel(api, cache) { signales += it }

    @Test fun `charge les echanges recus et envoyes dans le cache`() {
        val vm = vm()
        assertEquals(listOf(10L, 11L), vm.recues.value.map { it.id })
        assertEquals(listOf(20L), vm.envoyees.value.map { it.id })
        assertNull(vm.etat.value.erreur)
    }

    @Test fun `accepter appelle l'API puis notifie`() {
        coEvery { api.accepter(10) } returns echange(10, "ACCEPTED")
        vm().accepter(10)
        coVerify { api.accepter(10) }
        assertEquals(listOf(10L), signales)
    }

    @Test fun `proposer un creneau envoie la date au serveur`() {
        coEvery { api.proposerCreneau(10, any()) } returns echange(10, "TIME_PROPOSED")
        vm().proposerCreneau(10, "2027-01-05T14:00:00")
        coVerify { api.proposerCreneau(10, ProposeTimeRequest("2027-01-05T14:00:00")) }
        assertEquals(listOf(10L), signales)
    }

    @Test fun `accepter le nouveau creneau appelle accept-proposal`() {
        coEvery { api.accepterCreneau(20) } returns echange(20, "ACCEPTED")
        vm().accepterCreneau(20)
        coVerify { api.accepterCreneau(20) }
    }

    @Test fun `une erreur reseau est exposee sans planter`() {
        coEvery { api.refuser(10) } throws IOException("réseau")
        val vm = vm()
        vm.refuser(10)
        assertTrue(vm.etat.value.erreur!!.contains("joindre"))
        assertTrue(signales.isEmpty())
        vm.effacerErreur()
        assertNull(vm.etat.value.erreur)
    }

    @Test fun `hors ligne - la liste en cache reste disponible`() {
        val vm = vm()
        coEvery { api.recues() } throws IOException("réseau")
        vm.rafraichir()
        assertTrue(vm.etat.value.horsLigne)
        assertEquals(2, vm.recues.value.size)
    }
}

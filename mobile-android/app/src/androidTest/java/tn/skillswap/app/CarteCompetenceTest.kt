package tn.skillswap.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import tn.skillswap.app.data.CategorieInfo
import tn.skillswap.app.data.Competence
import tn.skillswap.app.ui.screens.BandeauHorsLigne
import tn.skillswap.app.ui.screens.CarteCompetence
import tn.skillswap.app.ui.theme.SkillSwapTheme

/** Test d'interface (instrumenté) : s'exécute sur émulateur ou téléphone. */
class CarteCompetenceTest {
    @get:Rule val rule = createComposeRule()

    private val angular = Competence(4, "Cours d'Angular", "Composants et services", "AVANCE", null, "VALIDEE", CategorieInfo(1, "Informatique"))

    @Test fun lacarteAfficheLeTitreLeNiveauEtLaCategorie() {
        rule.setContent { SkillSwapTheme { CarteCompetence(angular, onClick = {}) } }
        rule.onNodeWithText("Cours d'Angular").assertIsDisplayed()
        rule.onNodeWithText("Informatique").assertIsDisplayed()
        rule.onNodeWithText("Avancé").assertIsDisplayed()
    }

    @Test fun unClicSurLaCarteTransmetLIdentifiant() {
        var clique = -1L
        rule.setContent { SkillSwapTheme { CarteCompetence(angular, onClick = { clique = it }) } }
        rule.onNodeWithText("Cours d'Angular").performClick()
        assertEquals(4L, clique)
    }

    @Test fun leBandeauHorsLigneNApparaitQueSiNecessaire() {
        var visible = true
        rule.setContent { SkillSwapTheme { BandeauHorsLigne(visible) } }
        rule.onNodeWithText("Mode hors ligne", substring = true).assertIsDisplayed()
    }
}

package tn.skillswap.app

import org.junit.Assert.assertEquals
import org.junit.Test
import tn.skillswap.app.data.messageDepuisReponse
import tn.skillswap.app.data.roleDepuisJwt
import java.util.Base64

class KeycloakTest {
    /** Fabrique un faux jeton JWT (en-tête.charge.signature) avec les rôles donnés. */
    private fun jwt(vararg roles: String): String {
        val b64 = Base64.getUrlEncoder().withoutPadding()
        val charge = """{"sub":"1","realm_access":{"roles":[${roles.joinToString(",") { "\"$it\"" }}]}}"""
        return b64.encodeToString("""{"alg":"none"}""".toByteArray()) + "." + b64.encodeToString(charge.toByteArray()) + ".sig"
    }

    @Test fun `le role admin l'emporte sur les autres`() {
        assertEquals("ADMIN", roleDepuisJwt(jwt("offline_access", "ETUDIANT", "ADMIN", "MODERATEUR")))
    }

    @Test fun `le role moderateur est reconnu`() {
        assertEquals("MODERATEUR", roleDepuisJwt(jwt("MODERATEUR")))
    }

    @Test fun `sans role applicatif on est etudiant`() {
        assertEquals("ETUDIANT", roleDepuisJwt(jwt("default-roles-skillswap", "offline_access")))
        assertEquals("ETUDIANT", roleDepuisJwt(jwt("admin")))   // « admin » en minuscules n'est pas ADMIN
    }

    @Test fun `un jeton illisible donne etudiant sans planter`() {
        assertEquals("ETUDIANT", roleDepuisJwt("pas-un-jwt"))
        assertEquals("ETUDIANT", roleDepuisJwt("a.b.c"))
        assertEquals("ETUDIANT", roleDepuisJwt(""))
    }

    @Test fun `les erreurs Keycloak sont traduites`() {
        assertEquals("Email ou mot de passe incorrect.",
            messageDepuisReponse(401, """{"error":"invalid_grant","error_description":"Invalid user credentials"}"""))
        assertEquals("Compte incomplet : renseignez prénom, nom et email dans Keycloak, puis réessayez.",
            messageDepuisReponse(400, """{"error":"invalid_grant","error_description":"Account is not fully set up"}"""))
    }

    @Test fun `les erreurs du backend utilisent le champ message ou le code`() {
        assertEquals("Solde insuffisant", messageDepuisReponse(409, """{"message":"Solde insuffisant"}"""))
        assertEquals("Identifiants invalides ou session expirée.", messageDepuisReponse(401, ""))
        assertEquals("Action non autorisée.", messageDepuisReponse(403, ""))
        assertEquals("Erreur du serveur (500).", messageDepuisReponse(500, "{}"))
    }
}

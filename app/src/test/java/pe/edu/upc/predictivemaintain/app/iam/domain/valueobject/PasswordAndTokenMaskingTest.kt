package pe.edu.upc.predictivemaintain.app.iam.domain.valueobject

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PasswordAndTokenMaskingTest {

    @Test
    fun `Password toString does not contain raw password`() {
        val rawPassword = "SuperSecretPassword123!"
        val password = Password(rawPassword)

        val stringResult = password.toString()

        assertFalse("toString must not contain raw password", stringResult.contains(rawPassword))
        assertEquals("Password(***)", stringResult)
    }

    @Test
    fun `AccessToken toString does not contain raw token`() {
        val rawToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.SecretTokenContent"
        val accessToken = AccessToken(rawToken)

        val stringResult = accessToken.toString()

        assertFalse("toString must not contain raw token", stringResult.contains(rawToken))
        assertEquals("AccessToken(***)", stringResult)
    }
}

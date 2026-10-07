package pe.edu.upc.predictivemaintain.app.iam.domain.valueobject

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailTest {

    @Test
    fun `valid emails pass validation and trim whitespace`() {
        val validEmailStr = "  user@plant.com  "
        assertTrue(Email.isValid(validEmailStr))

        val email = Email(validEmailStr.trim())
        assertEquals("user@plant.com", email.value)
    }

    @Test
    fun `invalid emails fail validation`() {
        assertFalse(Email.isValid(""))
        assertFalse(Email.isValid("   "))
        assertFalse(Email.isValid("plainaddress"))
        assertFalse(Email.isValid("#@%^%#$@#$@#.com"))
        assertFalse(Email.isValid("@example.com"))
        assertFalse(Email.isValid("Joe Smith <email@example.com>"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructing Email with invalid format throws exception`() {
        Email("invalid-email")
    }

    @Test
    fun `createOrNull returns null for invalid email`() {
        assertNull(Email.createOrNull("bad-email"))
    }
}

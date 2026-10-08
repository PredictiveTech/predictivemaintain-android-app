package pe.edu.upc.predictivemaintain.app.iam.infrastructure.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.UserDto
import pe.edu.upc.predictivemaintain.app.iam.infrastructure.remote.UserPageDto

class TechnicianMapperTest {

    @Test
    fun `toDomain with valid UserDto maps successfully`() {
        val dto = UserDto(
            id = "tech-1",
            tenantId = "tenant-1",
            email = "tech@plant.com",
            displayName = "Tech One",
            active = true,
            roles = listOf("TECHNICIAN")
        )

        val result = TechnicianMapper.toDomain(dto)

        assertTrue(result is Outcome.Success)
        val tech = (result as Outcome.Success).data
        assertEquals("tech-1", tech.id.value)
        assertEquals("Tech One", tech.displayName.value)
        assertEquals("tech@plant.com", tech.email.value)
    }

    @Test
    fun `toDomain with blank id fails with InvalidResponse`() {
        val dto = UserDto(
            id = "",
            tenantId = "tenant-1",
            email = "tech@plant.com",
            displayName = "Tech One",
            active = true,
            roles = listOf("TECHNICIAN")
        )

        val result = TechnicianMapper.toDomain(dto)

        assertTrue(result is Outcome.Failure)
        val error = (result as Outcome.Failure).error
        assertTrue(error is AppError.InvalidResponse)
        assertEquals("id", (error as AppError.InvalidResponse).field)
    }

    @Test
    fun `toDomain with UserPageDto maps list of technicians`() {
        val pageDto = UserPageDto(
            items = listOf(
                UserDto(
                    id = "tech-1",
                    tenantId = "tenant-1",
                    email = "tech1@plant.com",
                    displayName = "Tech One",
                    active = true,
                    roles = listOf("TECHNICIAN")
                ),
                UserDto(
                    id = "tech-2",
                    tenantId = "tenant-1",
                    email = "tech2@plant.com",
                    displayName = "Tech Two",
                    active = true,
                    roles = listOf("TECHNICIAN")
                )
            ),
            totalElements = 2,
            page = 0,
            size = 10,
            totalPages = 1
        )

        val result = TechnicianMapper.toDomain(pageDto)

        assertTrue(result is Outcome.Success)
        val list = (result as Outcome.Success).data
        assertEquals(2, list.size)
        assertEquals("tech-1", list[0].id.value)
        assertEquals("tech-2", list[1].id.value)
    }
}

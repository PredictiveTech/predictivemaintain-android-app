package pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.mapper

import pe.edu.upc.predictivemaintain.app.core.error.AppError
import pe.edu.upc.predictivemaintain.app.core.error.Outcome
import pe.edu.upc.predictivemaintain.app.core.paging.PageResult
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.Asset
import pe.edu.upc.predictivemaintain.app.maintenance.domain.entity.AssetDetail
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetCode
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetId
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.AssetStatus
import pe.edu.upc.predictivemaintain.app.maintenance.domain.valueobject.Criticality
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.AssetDetailDto
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.AssetDto
import pe.edu.upc.predictivemaintain.app.maintenance.infrastructure.remote.AssetPageDto

object AssetMapper {

    fun toDomain(dto: AssetDto): Outcome<Asset> = mapping {
        Asset(
            id = field("id") { AssetId(dto.id) },
            code = field("code") { AssetCode(dto.code) },
            name = field("name") {
                require(dto.name.isNotBlank()) { "Asset name cannot be blank" }
                dto.name
            },
            location = dto.location,
            productionLine = dto.productionLine,
            assetType = field("assetType") {
                require(dto.assetType.isNotBlank()) { "Asset type cannot be blank" }
                dto.assetType
            },
            criticality = Criticality.fromString(dto.criticality),
            active = dto.active,
            status = AssetStatus.fromString(dto.status),
            sensorTypes = dto.sensorTypes
        )
    }

    fun toDomain(dto: AssetDetailDto): Outcome<AssetDetail> = mapping {
        AssetDetail(
            id = field("id") { AssetId(dto.id) },
            code = field("code") { AssetCode(dto.code) },
            name = field("name") {
                require(dto.name.isNotBlank()) { "Asset name cannot be blank" }
                dto.name
            },
            location = dto.location,
            productionLine = dto.productionLine,
            assetType = field("assetType") {
                require(dto.assetType.isNotBlank()) { "Asset type cannot be blank" }
                dto.assetType
            },
            criticality = Criticality.fromString(dto.criticality),
            latitude = dto.latitude,
            longitude = dto.longitude,
            active = dto.active
        )
    }

    fun toDomain(dto: AssetPageDto): Outcome<PageResult<Asset>> = mapping {
        val mappedItems = dto.items.map { itemDto ->
            when (val result = toDomain(itemDto)) {
                is Outcome.Success -> result.data
                is Outcome.Failure -> throw InvalidField(when (val err = result.error) {
                    is AppError.InvalidResponse -> err.field
                    else -> "items"
                })
            }
        }
        PageResult(
            items = mappedItems,
            page = dto.page,
            totalPages = dto.totalPages,
            totalElements = dto.totalElements
        )
    }

    private class InvalidField(val field: String) : RuntimeException("Invalid field: $field")

    private inline fun <T> field(name: String, build: () -> T): T {
        try {
            return build()
        } catch (_: IllegalArgumentException) {
            throw InvalidField(name)
        }
    }

    private inline fun <T> mapping(build: () -> T): Outcome<T> {
        return try {
            Outcome.Success(build())
        } catch (e: InvalidField) {
            Outcome.Failure(AppError.InvalidResponse(e.field))
        }
    }
}

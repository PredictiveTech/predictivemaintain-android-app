package pe.edu.upc.predictivemaintain.app.core.paging

data class PageResult<T>(
    val items: List<T>,
    val page: Int,
    val totalPages: Int,
    val totalElements: Long
) {
    val hasMore: Boolean
        get() = page + 1 < totalPages && totalPages > 0
}

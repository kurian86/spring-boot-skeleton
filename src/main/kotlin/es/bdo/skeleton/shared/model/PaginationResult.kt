package es.bdo.skeleton.shared.model

data class PaginationResult<T>(
    val totalCount: Long = 0,
    val items: List<T> = emptyList(),
    val pageInfo: PageInfo = PageInfo(),
) {

    companion object {
        fun <T> from(totalCount: Long, items: List<T>, offset: Long = 0L, limit: Int = 10): PaginationResult<T> {
            val hasNextPage = if (offset > 0) offset + limit < totalCount else limit < totalCount
            val hasPreviousPage = offset > 0

            return PaginationResult(totalCount, items, PageInfo(hasNextPage, hasPreviousPage))
        }
    }

    data class PageInfo(
        val hasNextPage: Boolean = false,
        val hasPreviousPage: Boolean = false,
    )
}

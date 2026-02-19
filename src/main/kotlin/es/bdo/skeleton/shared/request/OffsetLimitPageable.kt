package es.bdo.skeleton.shared.request

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

class OffsetLimitPageable(
    private val offset: Long,
    private val limit: Int = DEFAULT_LIMIT,
    private val sort: Sort = Sort.unsorted(),
) : Pageable {

    companion object {
        const val DEFAULT_LIMIT = 10
    }

    init {
        require(offset >= 0) { "Offset must not be less than zero!" }
        require(limit >= 1) { "Page size must not be less than one!" }
    }

    override fun getPageNumber(): Int {
        return offset.toInt() / limit
    }

    override fun getPageSize(): Int {
        return limit
    }

    override fun getOffset(): Long {
        return offset
    }

    override fun getSort(): Sort {
        return sort
    }

    override fun next(): Pageable {
        return OffsetLimitPageable(offset + limit, limit, sort)
    }

    override fun previousOrFirst(): Pageable {
        if (hasPrevious()) {
            return OffsetLimitPageable(offset - limit, limit, sort)
        }

        return first()
    }

    override fun first(): Pageable {
        return OffsetLimitPageable(0, limit, sort)
    }

    override fun withPage(pageNumber: Int): Pageable {
        return OffsetLimitPageable((pageNumber * limit).toLong(), limit, sort)
    }

    override fun hasPrevious(): Boolean {
        return offset >= limit
    }
}

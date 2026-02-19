package es.bdo.skeleton.user.infrastructure.specification

import es.bdo.skeleton.shared.model.Filter
import es.bdo.skeleton.shared.model.FilterGroup
import es.bdo.skeleton.shared.model.Operator
import es.bdo.skeleton.user.domain.UserStatus
import es.bdo.skeleton.user.infrastructure.model.UserEntity
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component

@Component
class UserFilterSpecification {

    fun toSpecification(filters: List<FilterGroup>): Specification<UserEntity>? {
        if (filters.isEmpty()) return null

        // Validate filters early to fail fast
        filters.flatMap { it.filters }.forEach { filter ->
            validateFilter(filter)
        }

        return Specification { root, query, cb ->
            val predicates = filters.map { filterGroup ->
                buildFilterGroupPredicate(filterGroup, root, cb)
            }

            cb.and(*predicates.toTypedArray())
        }
    }

    private fun validateFilter(filter: Filter) {
        val validProperties = setOf("id", "name", "email", "status")
        if (filter.property !in validProperties) {
            throw IllegalArgumentException("Property ${filter.property} not filterable")
        }

        // Validate operator-property combinations
        when (filter.operator) {
            Operator.LIKE -> {
                if (filter.property !in setOf("name", "email")) {
                    throw IllegalArgumentException("Operator LIKE not supported for property ${filter.property}")
                }
            }
            Operator.GREATER_THAN, Operator.LESS_THAN -> {
                throw IllegalArgumentException("Operator ${filter.operator} not yet implemented")
            }
            else -> { /* Other operators are fine */ }
        }
    }

    private fun buildFilterGroupPredicate(
        filterGroup: FilterGroup,
        root: jakarta.persistence.criteria.Root<UserEntity>,
        cb: jakarta.persistence.criteria.CriteriaBuilder
    ): Predicate {
        val filterPredicates = filterGroup.filters.map { filter ->
            buildFilterPredicate(filter, root, cb)
        }

        return if (filterPredicates.size == 1) {
            filterPredicates.first()
        } else {
            cb.or(*filterPredicates.toTypedArray())
        }
    }

    private fun buildFilterPredicate(
        filter: Filter,
        root: jakarta.persistence.criteria.Root<UserEntity>,
        cb: jakarta.persistence.criteria.CriteriaBuilder
    ): Predicate {
        return when (filter.operator) {
            Operator.EQUALITY -> buildEqualityPredicate(filter, root, cb)
            Operator.LIKE -> buildLikePredicate(filter, root, cb)
            Operator.GREATER_THAN -> buildGreaterThanPredicate(filter, root, cb)
            Operator.LESS_THAN -> buildLessThanPredicate(filter, root, cb)
            else -> throw IllegalArgumentException("Operator ${filter.operator} not supported for property ${filter.property}")
        }
    }

    private fun buildEqualityPredicate(
        filter: Filter,
        root: jakarta.persistence.criteria.Root<UserEntity>,
        cb: jakarta.persistence.criteria.CriteriaBuilder
    ): Predicate {
        return when (filter.property) {
            "id" -> cb.equal(root.get<java.util.UUID>("id"), java.util.UUID.fromString(filter.value))
            "status" -> cb.equal(root.get<UserStatus>("status"), UserStatus.valueOf(filter.value))
            "name" -> cb.equal(root.get<String>("name"), filter.value)
            "email" -> cb.equal(root.get<String>("email"), filter.value)
            else -> throw IllegalArgumentException("Property ${filter.property} not filterable")
        }
    }

    private fun buildLikePredicate(
        filter: Filter,
        root: jakarta.persistence.criteria.Root<UserEntity>,
        cb: jakarta.persistence.criteria.CriteriaBuilder
    ): Predicate {
        return when (filter.property) {
            "name" -> cb.like(cb.lower(root.get("name")), "%${filter.value.lowercase()}%")
            "email" -> cb.like(cb.lower(root.get("email")), "%${filter.value.lowercase()}%")
            else -> throw IllegalArgumentException("Operator LIKE not supported for property ${filter.property}")
        }
    }

    private fun buildGreaterThanPredicate(
        filter: Filter,
        root: jakarta.persistence.criteria.Root<UserEntity>,
        cb: jakarta.persistence.criteria.CriteriaBuilder
    ): Predicate {
        throw IllegalArgumentException("GREATER_THAN not yet implemented")
    }

    private fun buildLessThanPredicate(
        filter: Filter,
        root: jakarta.persistence.criteria.Root<UserEntity>,
        cb: jakarta.persistence.criteria.CriteriaBuilder
    ): Predicate {
        throw IllegalArgumentException("LESS_THAN not yet implemented")
    }
}

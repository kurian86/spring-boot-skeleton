package es.bdo.skeleton.shared.model

import org.springframework.data.domain.Sort as SpringSort

data class Sort(
    val property: String,
    val direction: SpringSort.Direction,
)

package es.bdo.skeleton.shared.model

data class Filter(
    val property: String,
    val value: String,
    val operator: Operator = Operator.EQUALITY,
)

package es.bdo.skeleton.tenant.application.exception

class TenantNotFoundException(message: String = "Tenant not found") : RuntimeException(message)

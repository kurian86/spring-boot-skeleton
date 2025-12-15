package es.bdo.skeleton

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import org.junit.jupiter.api.Test

class ArchitectureTests {
    private val classes: JavaClasses = ClassFileImporter()
        .withImportOption(ImportOption.DoNotIncludeTests())
        .importPackages("es.bdo.skeleton")

    @Test
    fun `Modules can only access other modules through application layer`() {
        noClasses()
            .that().resideInAPackage("..user..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..absence.domain..", "..absence.infrastructure..")
            .check(classes)
        
        noClasses()
            .that().resideInAPackage("..absence..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..user.domain..", "..user.infrastructure..")
            .check(classes)
    }

    @Test
    fun `Domain layer should not depend on other layers`() {
        layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")
            .whereLayer("Domain").mayNotAccessAnyLayer()
            .check(classes)
    }

    @Test
    fun `Application layer should only depend on domain`() {
        layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")
            .whereLayer("Application").mayOnlyAccessLayers("Domain")
            .check(classes)
    }

    @Test
    fun `Infrastructure layer can depend on domain and application`() {
        layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")
            .whereLayer("Infrastructure").mayOnlyAccessLayers("Domain", "Application")
            .check(classes)
    }
}

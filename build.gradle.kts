plugins {
	kotlin("jvm") version "2.3.0-RC3"
	kotlin("plugin.spring") version "2.3.0-RC3"
	id("org.springframework.boot") version "4.0.0"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.graalvm.buildtools.native") version "0.11.3"

	id("org.flywaydb.flyway") version "11.14.1"
	id("nu.studer.jooq") version "9.0"
}

buildscript {
	dependencies {
		classpath("org.postgresql:postgresql:42.7.8")
		classpath("org.flywaydb:flyway-database-postgresql:11.14.1")
	}
}

group = "es.bdo"
version = "0.0.1-SNAPSHOT"
description = "Demo architecture for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

extra["springModulithVersion"] = "2.0.0"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.springframework.boot:spring-boot-starter-jooq")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.modulith:spring-modulith-starter-core")
	implementation("org.springframework.modulith:spring-modulith-starter-jdbc")
	implementation("tools.jackson.module:jackson-module-kotlin")
	implementation("jakarta.xml.bind:jakarta.xml.bind-api")
	implementation("com.github.ben-manes.caffeine:caffeine")
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("com.sun.xml.bind:jaxb-impl")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
	testImplementation("org.springframework.boot:spring-boot-starter-jooq-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.modulith:spring-modulith-starter-test")
	testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	jooqGenerator("org.postgresql:postgresql")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.modulith:spring-modulith-bom:${property("springModulithVersion")}")
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

val dbDriver = project.properties["dbDriver"] as String
val dbUrl = project.properties["dbUrl"] as String
val dbUsername = project.properties["dbUsername"] as String
val dbPassword = project.properties["dbPassword"] as String

jooq {
	version.set("3.19.28")
	configurations {
		create("main") {
			generateSchemaSourceOnCompilation.set(false)
			jooqConfiguration.apply {
				logging = org.jooq.meta.jaxb.Logging.WARN
				jdbc.apply {
					driver = dbDriver
					url = dbUrl
					user = dbUsername
					password = dbPassword
				}

				generator.apply {
					name = "org.jooq.codegen.KotlinGenerator"
					database.apply {
						name = "org.jooq.meta.postgres.PostgresDatabase"
						inputSchema = "public"
						excludes = "flyway_schema_history"
					}
					generate.apply {
						isDeprecated = false
						isRecords = true
						isImmutablePojos = true
						isFluentSetters = true
						isPojosAsKotlinDataClasses = true
						isKotlinNotNullPojoAttributes = true
						isKotlinNotNullRecordAttributes = true
						isKotlinNotNullInterfaceAttributes = true
					}
					target.apply {
						packageName = "jooq.generated"
						directory = "build/generated/jooq"
					}
				}
			}
		}
	}
}

val flywayTemplateMigrate by tasks.registering(org.flywaydb.gradle.task.FlywayMigrateTask::class) {
	driver = dbDriver
	url = dbUrl
	user = dbUsername
	password = dbPassword

	locations = arrayOf("classpath:db/migration/tenant")

	baselineOnMigrate = true
	cleanDisabled = false
}

tasks.named("generateJooq") {
	dependsOn(flywayTemplateMigrate)
}

tasks.named("compileKotlin") {
	dependsOn("generateJooq")
}

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
    application
}

group = "com"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("Application")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // logging
    implementation("org.slf4j:slf4j-api:1.7.29")
    implementation("org.slf4j:slf4j-simple:1.7.29")

    // json
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.1")

    // test dependencies
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("io.mockk:mockk:1.12.3")
    testImplementation("org.mockito:mockito-core:4.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    testImplementation(platform("org.junit:junit-bom:5.6.0"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.6.0")

}

tasks {
    val fatJar = register<Jar>("fatJar") {
        dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources")) // We need this for Gradle optimization to work
        archiveClassifier.set("standalone") // Naming the jar
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes(mapOf("Main-Class" to application.mainClass)) } // Provided we set it up in the application plugin configuration
        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } +
                sourcesMain.output
        from(contents)
    }
    build {
        dependsOn(fatJar) // Trigger fat jar creation during build
    }
    test {
        useJUnitPlatform()
        dependsOn("cleanTest")
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
    withType<KotlinCompile>() {
        kotlinOptions.jvmTarget = "11"
    }
}
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.5.21" apply false
}

allprojects {
    group = "com"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

}

subprojects {
    apply {
        plugin("java")
        plugin("kotlin")
        plugin("idea")
    }

    dependencies {
        testImplementation(kotlin("test"))

        // kotlin
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.2")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.2")

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
        withType<Test> {
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
}


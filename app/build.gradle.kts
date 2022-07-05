plugins {
    application
}

application {
    mainClass.set("com.addi.application.LeadEvaluationApplication")
}

dependencies {
    implementation(project(":business"))
    implementation(project(":evaluator"))
    implementation(project(":third-party"))
    implementation("org.junit.jupiter:junit-jupiter:5.7.0")
    implementation("org.mock-server:mockserver-netty:5.13.2")
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
}
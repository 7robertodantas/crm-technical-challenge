rootProject.name = "empty-kotlin"

include("app")
include("business")
include("third-party")

project(":app").projectDir = file("app")
project(":business").projectDir = file("business")
project(":third-party").projectDir = file("third-party")
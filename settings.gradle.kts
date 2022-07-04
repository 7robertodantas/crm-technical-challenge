rootProject.name = "empty-kotlin"

include("app")
include("evaluator")
include("business")
include("third-party")

project(":app").projectDir = file("app")
project(":evaluator").projectDir = file("evaluator")
project(":business").projectDir = file("business")
project(":third-party").projectDir = file("third-party")
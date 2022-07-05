# Project Structure 

The following diagram represents an overview of the project structure.

<p align="center">
  <img src="docs/assets/crm-technical-challenge.drawio.svg">
</p>

### Modules

| Modules | Description |
|:--------------|:------------|
| **app** | This module contains the entrypoint of the application. It's responsible to instantiate all dependencies and glue them together to execute the business logic. |
| **business** | This module contains the implementation of business logic. It performs the validations and aggregations over adapter interfaces. |
| **evaluator** | This module contains a common logic to handle a chain of responsability. It contains boilerplate to run a pipeline of steps. |
| **third-party** | This module contains the http client requests implementation. It's responsible for doing the tcp connection. |
 
# Solution

## Assumptions

- I have assumed that the entrypoint (pipeline to convert lead into prospect) would take as input the national identification number of the person and it would process it by fetching its data from the local database repository and external systems
- I have assumed all external systems would also identify the "lead" by its national identification number, though that could be changed to something else, if needed.
- I have assumed fictional endpoints for national registry system, judicial record system and prospect qualification system.
- I have assumed that one key aspect of the implementation would be to make the validation process flexible, so we could change later the steps, adding new ones, removing, or changing the order of execution.

## Decisions

- I have decided to separate the project in different modules to group different layers of concern in each. 
    - The `app` module is concerned to the application main entrypoint. It should gather the dependencies, handle with configuration and app runner.
    - The `evaluator` module is concerned with a common evaluator "processor" that knows how to chain many steps to evaluate something and return an EvaluationOutcome as return.
  - The `business` module is where it is resides the custom steps to validate the national registry, judicial records and the call to qualification score system. It contains the business domain.
  - The `third-party` is where it resides the http client implementation, it defines concrete implementations for the external systems adapters interfaces.
- I have decided to create a custom pipeline mechanism that could allow us to easily change the order of execution of many steps without need to change the implementation.
  - There's an interface called `EvaluatorStep` that describes the method to evaluate a single step.
  - There's a `ParallelPipelineStep` and `SequentialPipelineStep` that can compose many steps, and it's method knows how to chain them together.
  
## Improvements

- The current solution lacks of a friendly user interface.
  - The current application is getting the national id number of the lead to process via command line arguments. 
  - We could implement a user interface, or at least give it some output to terminal.
- The pipeline process considers an evaluation of a single lead at the time, we could improve it to perform batch evaluation.
  - Perhaps merging steps to fetch many lead data from external systems or even use queues.

# Running Instructions 

There are a few configurations that can be tweaked via enviornment variables to run this project. The following section describes them.

## Configuration

| environment variable | default | behavior  
|---|---|---|
| **EMBEDDED_MOCKSERVER_STUB** | true | If enabled this will run a stub http server that will replace all external system URL integration and response back with a successful response.
| **NATIONAL_REGISTRY_URL** | http://localhost:8080/national-registry | It changes the endpoint to perform national registry http requests.
| **JUDICIAL_ARCHIVE_URL** | http://localhost:8080/judicial-archive |  It changes the endpoint to perform judicial archive http requests.
| **PROSPECT_QUALIFIER_URL** | http://localhost:8080/prospect-qualifier |  It changes the endpoint to perform prospect score qualification http requests.



## Build and run via Command Line

### Running via command line

```bash
./gradlew clean fatJar
java -jar ./app/build/libs/app-1.0-SNAPSHOT-standalone.jar <national-id-number>
```

> **_NOTE:_** 
> The `<national-id-number>` should be changed to the national identification number of the lead that will be processed.
> 
> Currently there's a fallback to a random UUID just to show that the program works.

## Build and run via Dockerfile 

### Running via docker
```bash
docker build . -t app 
docker run --interactive --tty --rm app <national-id-number>
```

## Running Tests

### Running tests via command line

```bash
./gradlew test
```

### Running tests via docker

```bash
docker run --rm -v $(pwd):/home/gradle/src gradle:6.9.2-jdk11 gradle --no-daemon -p /home/gradle/src test
```
**Important:** Need to execute on project root folder so that the $(pwd) will pick the correct folder.

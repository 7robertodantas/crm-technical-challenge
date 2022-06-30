# Running Instructions 
## Build and run via Command Line

### Running via command line

```bash
./gradlew clean fatJar
java -jar ./build/libs/empty-kotlin-1.0-SNAPSHOT-standalone.jar
```

## Build and run via Dockerfile 

### Running via docker
```bash
docker build . -t empty-kotlin
docker run --interactive --tty --rm empty-kotlin
```

## Running Tests

### Running tests via command line

```bash
./gradlew test
```

### Running tests via docker

```bash
docker run --rm -v $(pwd):/home/gradle/src gradle:6.9.2-jdk11 gradle --no-deamon -p /home/gradle/src test
```
**Important:** Need to execute on project root folder so that the $(pwd) will pick the correct folder.

from gradle:6.9.2-jdk11 as build

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle fatJar --no-daemon

FROM openjdk:11.0.6-jdk-slim
RUN mkdir /app
COPY --from=build /home/gradle/src/app/build/libs/*.jar /app/empty-kotlin.jar
RUN ls /app/
ENTRYPOINT ["java", "-jar", "/app/empty-kotlin.jar"]


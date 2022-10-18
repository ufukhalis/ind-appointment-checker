FROM gradle:7.5.1-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM eclipse-temurin:17.0.4.1_1-jre

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/ /app/
ADD app/ind-appointment-checker-*.jar app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]
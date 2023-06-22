#
# Build stage
#
FROM maven:3.8.4-jdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

#
# Package stage
#
FROM adoptopenjdk:17-jre-hotspot
COPY --from=build /target/SmartContactManager-0.0.1-SNAPSHOT.jar demo.jar
# ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-jar","demo.jar"]
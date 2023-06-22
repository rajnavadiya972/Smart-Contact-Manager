#
# Build stage
#
FROM maven:4.0.0-jdk-17 AS build
COPY . .
RUN mvn clean package -Pprod -DskipTests

#
# Package stage
#
FROM openjdk:11-jdk-slim
COPY --from=build /target/SmartContactManager-0.0.1-SNAPSHOT.jar demo.jar
# ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-jar","demo.jar"]
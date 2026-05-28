FROM maven:3.9.5-eclipse-temurin-21 AS builder

WORKDIR /parent
COPY pom.xml .
RUN mvn install -N -q

WORKDIR /app
COPY diplom-participant-service/pom.xml .
RUN mvn dependency:go-offline -B

COPY diplom-participant-service/src ./src
RUN mvn clean package -DskipTests
RUN cp target/*.jar app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/app.jar app.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]

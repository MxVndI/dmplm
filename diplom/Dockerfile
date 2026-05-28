FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /parent
COPY pom.xml .
RUN mvn install -N -q

WORKDIR /app
COPY diplom/pom.xml .
RUN mvn dependency:go-offline -q

COPY diplom/src ./src
RUN mvn clean package -DskipTests -q
# Переименовываем собранный JAR (Spring Boot сделал его исполняемым)
RUN cp target/*.jar app.jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
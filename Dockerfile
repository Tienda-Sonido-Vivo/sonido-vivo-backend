# Compila todos los módulos una sola vez y reutiliza la capa Maven para cada imagen.
FROM maven:3.9.14-eclipse-temurin-21 AS build
WORKDIR /workspace
ENV MAVEN_OPTS="-Xms128m -Xmx768m"
COPY . .
RUN mvn -T 1 -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
ARG SERVICE
COPY --from=build /workspace/${SERVICE}/target/${SERVICE}-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

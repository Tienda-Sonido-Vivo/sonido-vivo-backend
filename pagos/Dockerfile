FROM maven:3.9.14-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl pagos -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/pagos/target/pagos-1.0.0.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]

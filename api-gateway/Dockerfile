FROM maven:3.9.14-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -pl api-gateway -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/api-gateway-1.0.0.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]

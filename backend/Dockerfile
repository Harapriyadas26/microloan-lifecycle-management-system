FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN groupadd --system microloan && useradd --system --gid microloan --create-home microloan && mkdir -p /app/uploads/kyc && chown -R microloan:microloan /app
COPY --from=build --chown=microloan:microloan /workspace/target/backend-0.0.1-SNAPSHOT.jar app.jar
USER microloan
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]

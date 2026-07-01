# Stage 1: Build
FROM eclipse-temurin:25-jdk AS build
WORKDIR /vera

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:25-jre-alpine
WORKDIR /vera

COPY --from=build /vera/target/*.jar vera.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Xmx350m", "-Xms200m", "-jar", "vera.jar"]
FROM eclipse-temurin:25-jdk AS build
WORKDIR /vera
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine
WORKDIR /vera
COPY --from=build /vera/target/*.jar vera.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx350m", "-Xms200m", "-jar", "vera.jar"]
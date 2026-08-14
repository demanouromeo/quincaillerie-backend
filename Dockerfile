# --- Etape 1 : build ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Cache les dependances Maven separement du code source pour accelerer les
# rebuilds (cette couche ne change que si pom.xml change).
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -o dependency:go-offline -q || ./mvnw -B dependency:go-offline -q

COPY src/ src/
RUN ./mvnw -B -DskipTests package -q && \
    cp target/*.jar app.jar

# --- Etape 2 : image d'execution ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/app.jar app.jar

# Railway fournit PORT dynamiquement (voir application.properties) ; 8080
# reste la valeur par defaut hors Railway.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

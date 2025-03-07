# Folosim o imagine de bază cu OpenJDK 17
FROM openjdk:17-jdk-slim AS build

# Instalăm Maven în imagine
RUN apt-get update && apt-get install -y maven

# Setăm directorul de lucru
WORKDIR /app

# Copiem fișierele de configurare Maven și codul sursă
COPY pom.xml ./
COPY src ./src

# Construim proiectul și generăm JAR-ul
RUN mvn clean package -DskipTests

# Etapa a doua: Imaginea finală pentru rulare
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copiem JAR-ul generat din etapa de build
COPY --from=build /app/target/*.jar app.jar

# Expunem portul
EXPOSE 8080

# Rulăm aplicația
ENTRYPOINT ["java", "-jar", "app.jar"]
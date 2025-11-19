# Etapa 1: construir el JAR con Gradle
FROM gradle:8.8-jdk21-alpine AS build

WORKDIR /home/gradle/project

# Copiamos todo el proyecto
COPY . .

# Construimos el JAR de Spring Boot
RUN gradle bootJar --no-daemon

# Etapa 2: imagen liviana solo con el JAR
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiamos el JAR generado en la etapa anterior
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

# Puerto que expone la app
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java","-jar","/app/app.jar"]

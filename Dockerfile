FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY . .
RUN ./gradlew bootJar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /build/build/libs/mailnotify-service-1.0.jar app.jar

ENV SERVER_PORT=25000

EXPOSE 25000

ENTRYPOINT ["java", "-jar", "app.jar"]
#----------빌드----------------------

FROM gradle:8.5-jdk21 AS builder

WORKDIR /app

COPY --chown=gradle:gradle . .

RUN gradle clean build -x test --no-daemon
#----------실행----------------------

FROM openjdk:21-ea-21-slim

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT [ "java","-jar", "app.jar" ]



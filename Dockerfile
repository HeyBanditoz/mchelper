# syntax=docker/dockerfile:1

FROM scratch AS build
WORKDIR /build

COPY ./build /build

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Install oxipng
RUN apk update
RUN apk add --no-cache oxipng

COPY --from=build /build/libs/io.banditoz.mchelper-all.jar bot.jar

ENTRYPOINT ["java", "--enable-preview", "-Xmx64M", "-Xms64M", "-jar", "bot.jar"]

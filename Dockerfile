# syntax=docker/dockerfile:1

FROM scratch AS build
WORKDIR /build

COPY ./build /build

FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app

# Install oxipng
RUN apk update
RUN apk add --no-cache oxipng units

COPY --from=build /build/libs/io.banditoz.mchelper-all.jar bot.jar

ENTRYPOINT ["java", "-jar", "bot.jar"]

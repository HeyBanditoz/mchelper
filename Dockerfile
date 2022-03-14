# syntax=docker/dockerfile:1

FROM gradle:7.4.0-jdk17-alpine AS build
WORKDIR /build

COPY . /build

# Required for createVersionFile Gradle task to run
RUN apk add git
RUN gradle shadowJar --no-daemon

FROM ibm-semeru-runtimes:open-17-jdk-centos7
WORKDIR /app

RUN yum install units
COPY --from=build /build/build/libs/*all.jar bot.jar

ENTRYPOINT ["java", "-jar", "bot.jar"]

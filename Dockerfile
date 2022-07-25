# syntax=docker/dockerfile:1

FROM scratch AS build
WORKDIR /build

COPY ./build /build

FROM ibm-semeru-runtimes:open-17-jdk-focal
WORKDIR /app

RUN apt-get update && apt-get --no-install-recommends install -y units && rm -rf /var/lib/apt/lists/*

# Install oxipng
RUN curl --request GET -sL \
--url "https://github.com/shssoichiro/oxipng/releases/download/v5.0.1/oxipng-5.0.1-x86_64-unknown-linux-musl.tar.gz" \
--output "/app/oxipng.tar.gz"
RUN echo "89240cfd863f8007ab3ad95d88dc2ce15fc003a0421508728d73fec1375f19b6  /app/oxipng.tar.gz" | sha256sum --check
RUN tar xvf /app/oxipng.tar.gz
RUN mv oxipng-5.0.1-x86_64-unknown-linux-musl/oxipng /usr/bin/oxipng
RUN rm -rf /app/oxipng.tar.gz /app/oxipng-5.0.1-x86_64-unknown-linux-musl

COPY --from=build /build/libs/io.banditoz.mchelper-all.jar bot.jar

ENTRYPOINT ["java", "--enable-preview", "-Xmx64M", "-Xms64M", "-jar", "bot.jar"]

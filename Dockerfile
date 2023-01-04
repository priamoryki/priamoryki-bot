FROM maven:3.6.3-openjdk-14-slim AS build

WORKDIR /usr/bin/priamoryki-bot

COPY src src
COPY pom.xml .

RUN mvn -f pom.xml clean package

FROM openjdk:14-slim

WORKDIR /usr/bin/priamoryki-bot

COPY --from=build /usr/bin/priamoryki-bot/target/DiscordBot-1.1.jar /usr/bin/priamoryki-bot/DiscordBot.jar
COPY data data
COPY sounds sounds
EXPOSE 1080
ENTRYPOINT ["java","-jar","DiscordBot.jar"]

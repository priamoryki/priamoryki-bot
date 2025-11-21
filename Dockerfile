FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /usr/bin/priamoryki-bot

COPY src src
COPY pom.xml .

RUN mvn -f pom.xml clean package

FROM eclipse-temurin:21

WORKDIR /usr/bin/priamoryki-bot

COPY --from=build /usr/bin/priamoryki-bot/target/DiscordBot-1.1.jar /usr/bin/priamoryki-bot/DiscordBot.jar
COPY data data
COPY sounds sounds
EXPOSE 1080
ENTRYPOINT ["java","-jar","DiscordBot.jar"]

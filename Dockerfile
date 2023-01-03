FROM maven:alpine AS build
COPY src /usr/bin/priamoryki-bot/src
COPY pom.xml /usr/bin/priamoryki-bot
COPY data /usr/bin/priamoryki-bot/data
COPY sounds /usr/bin/priamoryki-bot/sounds
RUN mvn -f /usr/bin/priamoryki-bot/pom.xml clean package

FROM openjdk:14-jre-slim
COPY --from=build /usr/bin/priamoryki-bot/target/DiscordBot-1.1.jar /usr/bin/priamoryki-bot/DiscordBot.jar
EXPOSE 1080
ENTRYPOINT ["java","-jar","/usr/bin/priamoryki-bot/DiscordBot.jar"]

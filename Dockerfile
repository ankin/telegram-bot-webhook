FROM eed3si9n/sbt:jdk11-alpine AS SBT_BUILD

# copy the src code to the container
COPY ./ /telegram-bot-webhook/

WORKDIR /telegram-bot-webhook

RUN sbt assembly



FROM adoptopenjdk/openjdk11:jdk-11.0.8_10-alpine

COPY --from=SBT_BUILD /telegram-bot-webhook/target/scala-2.13/telegram-bot-webhook*.jar /app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]
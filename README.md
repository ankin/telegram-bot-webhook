## Telegram Webhook 

Telegram webhook pet-app based on [htt4s](https://http4s.org/), [cats-effect](https://typelevel.org/cats-effect/) and [doobie](https://github.com/tpolecat/doobie) with PostgreSQL for persistence.  

To get it running:
- Build Docker image. It will also build an executable JAR with dockerized build (see [Dockerfile](./Dockerfile)).
- Prepare PostgresSQL database
- Run docker container and pass necessary environment variables to connect to your database see [application.conf](./src/main/resources/application.conf)
- It uses an _allowlist_ to configure, which chats will be supported. Can be set via environment variable `WEBHOOK_CHAT_ALLOWLIST` see [application.conf](./src/main/resources/application.conf)

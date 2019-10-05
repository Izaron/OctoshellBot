# Octoshell Telegram Bot

**Octoshell** is an access management system for HPC centers.
It is hosted on https://users.parallel.ru.

This project is a **Telegram bot** for Octoshell users.
You may access the bot by its username `@OctoshellBot` or by link [t.me/OctoshellBot](t.me/OctoshellBot)

This bot is written on **Java 8** and based mainly on Java Spring Framework, but also uses other libraries
like Apache Commons and TelegramBots.

### Features

Stay tuned for updates, not worth looking rn.

### How to start exploring and developing this project

0. Clone or download this project.
Open your local copy in any IDE that is able to handle Maven projects
(**Intellij Idea** is a fine option). The IDE will start downloading necessary dependencies automatically.

0. You'll probably need to install a plugin for Lombok compatibility (a cool library for fixing Java syntactical problems).
Read a manual [https://www.baeldung.com/lombok-ide](https://www.baeldung.com/lombok-ide).

0. Technically, you are free to launch the project immediately, but you firstly should
provide bot credentials. Open [src/main/resources/application.yml](src/main/resources/application.yml)
and enter the **token** and the **username** of the bot.
For testing purposes you can create your own bot, that will have almost the same functionality as the
"production" bot, but won't interfere with users.<br/><br/>
NEVER COMMIT YOUR TOKEN! Do amend commit if you did or create a new bot if you cannot.

0. If you live in a country which bans Telegram, you probably ought
to enter proxy credentials and set `use-proxy: true` when running locally.
Not all proxies are working, just try them until you find a working one.<br/><br/>
(Note to myself: When deploying the bot, the outer servers are highly likely to work without any proxy, need to check)

0. Run the project. It must be running without errors in console. The bot must be "alive" now
and every message logged in console.
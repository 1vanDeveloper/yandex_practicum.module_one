# Работа по Yandex Practicum Middle Java-разработчик

## Запуск тестов в IDE
Выполните команду в корне проекта:
```shell
docker-compose up --force-recreate --renew-anon-volumes -d
```
Поднимется контейнер СУБД PostgreSQL необходимый для интеграционного тестирования.
Docker-compose настроен так, что при запуске контейнера PostgreSQL будет инициализирована схема данных и некоторые данные для тестирования.

Скрипты sql находятся по адресу:
```shell
/scripts/schema.sql # схема БД
/scripts/data.sql # данные для наполнения БД
```

## Сборка и запуск приложения
На вашем рабочем месте должна быть установлена Java 25 как java по-умолчанию.
Для сборки `jar`-файла выполните команду в корне проекта:
```shell
mvn clean install
```
После выполнения команды будет собран файл приложения по адресу:
```shell
/target/yandex-0.0.1-SNAPSHOT.jar
```
Запустите приложение, выполнив команду:
```shell
java -jar target/yandex-0.0.1-SNAPSHOT.jar
```

## Примечания
`Swagger` после запуска приложения доступен по адресу: `http://localhost:8080/swagger-ui/index.html`.
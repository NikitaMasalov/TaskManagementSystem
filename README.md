# TaskManagementSystem

REST API для управления задачами на Java и Spring Boot. Приложение хранит данные в MySQL и предоставляет операции получения, создания, обновления и удаления задач, а также фильтрацию по пользователю.

Проект находится в разработке. В текущей версии создание задачи завершается ошибкой при формировании ответа — подробности приведены в разделе [«Известные ограничения»](#известные-ограничения).

## Технологии

| Технология | Назначение |
| --- | --- |
| Java 21 | Язык и платформа приложения |
| Spring Boot 4.0.8 | Конфигурация и запуск приложения |
| Spring Web MVC | REST-контроллеры и обработка HTTP-запросов |
| Spring Data JPA / Hibernate | Работа с сущностями и базой данных |
| MySQL / MySQL Connector/J | Хранение данных и JDBC-драйвер; запуск проверен с MySQL 8.4 |
| Maven 3.9.16 / Maven Wrapper | Управление зависимостями, сборка и тестирование |
| JUnit Jupiter / Spring Boot Test | Тестирование загрузки контекста приложения |
| Spring Boot DevTools | Инструменты для разработки |
| Lombok | Подключён в зависимостях и обработчиках аннотаций |

## Возможности и модель данных

- Получение списка задач и отдельной задачи по идентификатору.
- Фильтрация задач по `userId`.
- Обновление названия, описания и статуса задачи.
- Удаление задачи.
- Эндпоинт создания задачи; его текущее ограничение описано ниже.

Сущность `Task` содержит `id`, `title`, `description`, `status` и ссылку на `User`. Связь между пользователем и задачами — «один ко многим». Статус хранится как строка: значения `NEW` и `DONE` в примерах условные, фиксированного перечисления статусов в коде нет.

## Требования

- **JDK 21**, включая компилятор `javac`.
- Git.
- MySQL 8.4 либо Docker для запуска базы в контейнере.
- Доступ к Maven Central при первой сборке.

Отдельно устанавливать Maven не требуется: в репозитории есть Maven Wrapper. Команды ниже рассчитаны на Bash в Linux, macOS или WSL и выполняются из корня проекта. Для Windows предусмотрен `mvnw.cmd`; настройку переменных окружения нужно адаптировать под используемую оболочку.

## Локальный запуск

### 1. Получить проект

```bash
git clone https://github.com/NikitaMasalov/TaskManagementSystem.git
cd TaskManagementSystem
java -version
javac -version
```

Обе команды проверки Java должны показывать версию 21. Если задана переменная `JAVA_HOME`, она должна указывать на JDK 21.

### 2. Запустить MySQL

Если MySQL уже установлен, создайте базу `demo` и пользователя с правами на неё, затем переходите к настройке подключения. Для нового локального экземпляра можно использовать Docker:

```bash
read -rsp 'Пароль root для локальной MySQL: ' MYSQL_ROOT_PASSWORD
printf '\n'
read -rsp 'Пароль пользователя приложения: ' MYSQL_PASSWORD
printf '\n'
export MYSQL_ROOT_PASSWORD MYSQL_PASSWORD

docker run -d \
  --name taskmanagement-db \
  -p 127.0.0.1:3306:3306 \
  -e MYSQL_ROOT_PASSWORD \
  -e MYSQL_PASSWORD \
  -e MYSQL_DATABASE=demo \
  -e MYSQL_USER=taskapp \
  -v taskmanagement-mysql-data:/var/lib/mysql \
  mysql:8.4
```

Введите непустые пароли. Порт `3306` должен быть свободен. Данные сохраняются в Docker volume `taskmanagement-mysql-data`; переменные инициализации MySQL применяются только при первом запуске с пустым каталогом данных.

Дождитесь завершения инициализации и проверьте подключение:

```bash
docker exec taskmanagement-db sh -c \
  'MYSQL_PWD="$MYSQL_PASSWORD" mysql --protocol=TCP -h127.0.0.1 -u"$MYSQL_USER" "$MYSQL_DATABASE" -e "SELECT 1;"'
```

Если база ещё запускается, повторите проверку через несколько секунд. Для просмотра журналов используйте `docker logs taskmanagement-db`.

### 3. Настроить подключение

В том же терминале задайте переменные Spring Boot:

```bash
export SPRING_DATASOURCE_URL='jdbc:mysql://127.0.0.1:3306/demo'
export SPRING_DATASOURCE_USERNAME='taskapp'
export SPRING_DATASOURCE_PASSWORD="$MYSQL_PASSWORD"
export SERVER_ADDRESS='127.0.0.1'
unset MYSQL_ROOT_PASSWORD MYSQL_PASSWORD
```

Для собственной MySQL укажите её адрес, пользователя и пароль. В новом терминале переменные нужно задать заново; пароль можно прочитать через `read -rsp 'Пароль БД: ' SPRING_DATASOURCE_PASSWORD`, затем экспортировать переменную.

| Переменная | Назначение |
| --- | --- |
| `SPRING_DATASOURCE_URL` | JDBC-адрес базы данных |
| `SPRING_DATASOURCE_USERNAME` | Пользователь базы данных |
| `SPRING_DATASOURCE_PASSWORD` | Пароль пользователя базы данных |
| `SERVER_ADDRESS` | Адрес, на котором приложение принимает запросы |
| `SERVER_PORT` | HTTP-порт; по умолчанию `8080` |

Переменные окружения переопределяют настройки из `src/main/resources/application.yaml`. Не сохраняйте реальные пароли в репозитории. Приложение не загружает `.env` автоматически.

Hibernate создаёт и обновляет таблицы при запуске благодаря `spring.jpa.hibernate.ddl-auto=update`. Саму базу данных необходимо создать заранее.

### 4. Запустить приложение

Для разработки:

```bash
bash mvnw spring-boot:run
```

Либо собрать исполняемый JAR и запустить его:

```bash
bash mvnw clean verify
java -jar target/TaskManagementSystem-0.0.1-SNAPSHOT.jar
```

Сборка с `verify` выполняет тесты и требует доступной MySQL. Вариант с отдельной тестовой базой приведён ниже.

После сообщения `Started TaskManagementSystemApplication` проверьте API из другого терминала:

```bash
curl --fail --silent --show-error http://127.0.0.1:8080/task
```

Ожидается HTTP `200` и JSON-массив; для пустой базы — `[]`. Веб-интерфейс в проект не входит.

Для остановки приложения нажмите `Ctrl+C`. Базу можно остановить командой `docker stop taskmanagement-db`, а затем запустить снова через `docker start taskmanagement-db`.

## REST API

Базовый путь: `/task`. Тела запросов и ответов передаются в JSON.

| Метод | Путь | Описание |
| --- | --- | --- |
| `GET` | `/task` | Получить все задачи |
| `GET` | `/task?userId=1` | Получить задачи пользователя |
| `GET` | `/task/{id}` | Получить задачу по идентификатору |
| `POST` | `/task` | Создать задачу; сейчас возвращает `500`, см. ограничения |
| `POST` | `/task/{id}` | Обновить название, описание и статус задачи |
| `DELETE` | `/task/{id}` | Удалить задачу |

### Создание задачи

Пример текущего формата запроса. **В этой версии запрос сохраняет запись, но возвращает HTTP `500`**. Повторная отправка может создать дубликат.

```bash
curl -i -X POST http://127.0.0.1:8080/task \
  -H 'Content-Type: application/json' \
  -d '{"title":"Изучить Spring Boot","description":"Разобрать REST-контроллеры","status":"NEW","userId":null}'
```

Поля `title`, `description` и `status` соответствуют обязательным столбцам БД; максимальная длина `title` — 50 символов. Поле `userId` присутствует в DTO запроса, но пока не используется при создании сущности.

### Чтение, обновление и удаление

Вместо `1` подставьте идентификатор существующей задачи из ответа `GET /task`. При обновлении передавайте все три поля: частичное обновление не реализовано.

```bash
curl http://127.0.0.1:8080/task
curl http://127.0.0.1:8080/task/1
curl 'http://127.0.0.1:8080/task?userId=1'

curl -X POST http://127.0.0.1:8080/task/1 \
  -H 'Content-Type: application/json' \
  -d '{"title":"Изучить Spring Boot","description":"REST-контроллеры изучены","status":"DONE"}'

curl -i -X DELETE http://127.0.0.1:8080/task/1
```

## Тестирование

В репозитории есть один тест `contextLoads` с `@SpringBootTest`. Он проверяет загрузку контекста приложения и требует подключения к MySQL; это не полное покрытие REST API.

Для описанного выше Docker-контейнера создайте отдельную базу и выдайте пользователю приложения права на неё:

```bash
docker exec -i taskmanagement-db sh -c \
  'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot' <<'SQL'
CREATE DATABASE IF NOT EXISTS demo_test;
GRANT ALL PRIVILEGES ON demo_test.* TO 'taskapp'@'%';
SQL
```

Если используете собственную MySQL, выполните аналогичные действия от администратора БД. Затем запустите тесты, сохранив настроенные ранее имя пользователя и пароль:

```bash
SPRING_DATASOURCE_URL='jdbc:mysql://127.0.0.1:3306/demo_test' bash mvnw test
```

Полная сборка с тестами и упаковкой JAR:

```bash
SPRING_DATASOURCE_URL='jdbc:mysql://127.0.0.1:3306/demo_test' bash mvnw clean verify
```

Здесь URL переопределяется только на время команды; приложение продолжит использовать `demo`. Отчёты тестов находятся в `target/surefire-reports/`.

## Структура проекта

```text
src/
├── main/
│   ├── java/com/example/TaskManagementSystem/
│   │   ├── controller/   # HTTP-эндпоинты
│   │   ├── dto/          # Модели запросов и ответов
│   │   ├── entity/       # JPA-сущности Task и User
│   │   ├── mapper/       # Преобразование DTO и сущностей
│   │   ├── repository/   # Доступ к данным через Spring Data JPA
│   │   ├── service/      # Логика работы с задачами
│   │   └── TaskManagementSystemApplication.java
│   └── resources/
│       └── application.yaml
└── test/java/com/example/TaskManagementSystem/
    └── TaskManagementSystemApplicationTests.java
```

## Известные ограничения

- **Ошибка создания задачи.** `TaskMapper.fromCreateRequest` не назначает пользователя, а `TaskMapper.toResponse` вызывает `task.getUser().getId()`. Это приводит к `NullPointerException` и HTTP `500` после сохранения задачи.
- **Обработка отсутствующих задач.** Чтение и обновление несуществующей задачи выбрасывают `RuntimeException`; корректный ответ `404` пока не настроен.
- **Пользователи.** Сущность `User` есть, но отдельных REST-эндпоинтов управления пользователями нет.
- **Валидация и доступ.** Проверка входных DTO через Bean Validation, аутентификация и авторизация пока не реализованы.
- **Схема базы.** Отдельных миграций нет; используется Hibernate `ddl-auto=update`.

## Решение типичных проблем

| Проблема | Что проверить |
| --- | --- |
| Ошибка компиляции для Java 21 | Установлен ли полный JDK 21, что показывают `javac -version` и `bash mvnw -version`, куда указывает `JAVA_HOME` |
| `Communications link failure` / отказ в подключении | Запущена ли MySQL, доступен ли порт `3306`, верен ли JDBC URL |
| `Access denied for user` | Имя пользователя, пароль и права на выбранную базу; смена переменных контейнера не меняет пользователей в существующем volume |
| `Unknown database` | Создана ли база `demo` или `demo_test` до запуска приложения или тестов |
| Порт `8080` занят | Остановите другой процесс либо задайте `SERVER_PORT=8081` и используйте этот порт в запросах |
| Maven не загружает зависимости за прокси | Настройте прокси в пользовательском Maven `settings.xml`; Maven не всегда использует `HTTPS_PROXY` автоматически |

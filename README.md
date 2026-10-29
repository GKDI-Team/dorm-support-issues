# dorm-support-issues

## О сервисе

Сервис заявок жильцов: создание, просмотр и изменение статуса заявок.

**Технологии:** Java, Spring Boot, PostgreSQL, Kafka, JWT.

**Основные ручки через Gateway:**

- `GET /api/v1/categories` — категории заявок.
- `POST /api/v1/issues` — создание заявки.
- `GET /api/v1/issues` — список заявок.
- `GET /api/v1/issues/{issueId}` — заявка по идентификатору.
- `PATCH /api/v1/issues/{issueId}/status` — изменение статуса.

## Запуск

Из папки `dorm-support-infrastructure`:

```bash
docker compose up --build issues-service
```

Compose также запустит зависимости. Для доступа через Gateway поднимите весь проект командой `make up` из папки инфраструктуры.

## Документация API

При запущенном сервисе Swagger UI доступен по адресу `/swagger-ui.html`, а описание API — по адресу `/openapi.yaml`.

Описание событий Kafka находится в `src/main/resources/events.schema.json`.

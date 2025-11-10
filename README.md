# Bank Cards Project

## Описание

Проект **Bank Cards** предназначен для управления пользователями, картами и переводами между картами.  
Система поддерживает:

- Регистрацию и управление пользователями (CRUD)
- Создание и управление банковскими картами (CRUD)
- Переводы между картами с расчетом комиссии
- Поиск, фильтрацию и пагинацию данных

Проект реализован на Java с использованием Spring Boot и обеспечивает REST API для взаимодействия с фронтендом или внешними сервисами.

---

## Стек технологий

- **Backend:** Java 17+, Spring Boot, Spring Data JPA, Spring Security
- **База данных:** PostgreSQL
- **Сборка и управление зависимостями:** Maven
- **Документация API:** OpenAPI 3.0 (`openapi.yml`), Swagger UI
- **Контейнеризация:** Docker (опционально)

---

## Установка и запуск

### Клонирование репозитория

- git clone https://github.com/zoss75/Bank_REST

### Настройка базы данных

spring.datasource.url=jdbc:postgresql://localhost:5432/bankcards
spring.datasource.username=bank_user
spring.datasource.password=bank_pass
spring.jpa.hibernate.ddl-auto=update

### Сборка и запуск

mvn clean install

mvn spring-boot:run

Приложение будет доступно по адресу: http://localhost:8080

### Swagger UI

Для тестирования API откройте:
http://localhost:8080/swagger-ui.html

Документация API находится в файле README_Вoks.md
и спецификации openapi.yml

### API

Основные эндпоинты:

Пользователи (Users)

CRUD и поиск: /users, /users/{id}, /users/search?q=

Поддержка фильтрации по роли и пагинации

Карты (Cards)

CRUD и поиск: /cards, /cards/{id}, /cards/search?q=

Фильтры по владельцу и балансу, пагинация

Переводы (Transfers)

Перевод между картами: /transfers

Рассчитывается комиссия

### Примеры использования

Создание пользователя
curl -X POST "http://localhost:8080/users" \
-H "Content-Type: application/json" \
-d '{"username":"alice","password":"1234","role":"USER"}'

Создание карты
curl -X POST "http://localhost:8080/cards" \
-H "Content-Type: application/json" \
-d '{"number":"1111-2222-3333-4444","balance":1000,"ownerId":1}'

Перевод между картами
curl -X POST "http://localhost:8080/transfers" \
-H "Content-Type: application/json" \
-d '{"fromCardId":10,"toCardId":20,"amount":100}'

### Тестирование

Используйте Swagger UI для интерактивного тестирования API

Для unit-тестов: JUnit + Mockito

Примеры тестов находятся в src/test/java/com/example/bankcards

### Контакты / Автор

Автор: Сергей Зяблицкий

Email: zoss75@yandex.ru


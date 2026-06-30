# 📚 RAG Service

RAG-сервис с **OpenAI-совместимым API** для работы с документами и генерации ответов на основе базы знаний.

---

## ✨ Возможности

- 🤖 OpenAI-compatible API (`/v1/chat/completions`)
- 📄 Загрузка и хранение документов
- 🔍 Поиск релевантных фрагментов (RAG)
- 🧠 Генерация ответов на основе базы знаний
- 🗄️ Векторное хранилище **Qdrant**
- 🐳 Запуск через **Docker Compose**

---

# 🚀 Быстрый старт

## 1. Настройка

Скопируйте файл окружения:

```bash
cp .env.example .env
```

Добавьте API-ключ в `.env`:

```env
OLLAMA_API_KEY=your_key
```

---

## 2. Запуск

```bash
docker compose up -d
```

---

## 3. Проверка

Получить список доступных моделей:

```bash
curl http://localhost:8080/v1/models
```

Если сервис успешно запущен, вы получите список доступных моделей.

---

# 📖 API

| Метод | Endpoint | Описание |
|-------|----------|----------|
| `GET` | `/v1/models` | Получить список моделей |
| `POST` | `/v1/chat/completions` | Генерация ответа |
| `POST` | `/api/documents/upload` | Загрузка документа |
| `GET` | `/api/documents` | Получить список документов |
| `DELETE` | `/api/documents/{id}` | Удалить документ |

---

# 📑 Swagger UI

После запуска документация доступна по адресу:

**http://localhost:8080/swagger-ui.html**

---

# ⚙️ Конфигурация

## Обязательные параметры

```env
OLLAMA_API_KEY=your_key
```

## Дополнительные параметры

```env
OLLAMA_LLM_MODEL=glm-4.7
OLLAMA_EMBEDDING_MODEL=nomic-embed-text

RAG_CHUNK_SIZE=1000
RAG_CHUNK_OVERLAP=200

RAG_TOP_K=5
RAG_SIMILARITY_THRESHOLD=0.7
```

---

# 🏗️ Архитектура

```text
             +--------------------+
             |  OpenAI API Client |
             +---------+----------+
                       |
                       v
          /v1/chat/completions
                       |
                       v
               Spring Boot API
                       |
          +------------+------------+
          |                         |
          v                         v
    Embedding Model           LLM Model
          |                         |
          +------------+------------+
                       |
                       v
                   Qdrant
              (Vector Search)
                       |
                       v
                 Uploaded Docs
```

---

# 🛠️ Технологии

- ☕ Java 17
- 🌱 Spring Boot 3.2
- 🤖 Spring AI
- 🧠 Qdrant
- 🐘 PostgreSQL
- 🐳 Docker & Docker Compose

---

# 📄 Лицензия

Проект распространяется в соответствии с выбранной лицензией.
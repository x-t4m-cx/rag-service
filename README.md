# RAG Service

Production-ready сервис Retrieval-Augmented Generation (RAG) с OpenAI-совместимым REST API. Приложение индексирует загруженные документы в векторную базу Qdrant, выполняет семантический поиск по базе знаний и генерирует ответы через Ollama Cloud (модель `glm-4.7`).

Сервис разработан на Java 21 и Spring Boot 3.5 с использованием Spring AI. Поддерживает синхронную и потоковую (SSE) генерацию ответов, асинхронную обработку документов и опциональный веб-поиск через SearXNG.

---

## Возможности

- **OpenAI-compatible API** — эндпоинты `/v1/models` и `/v1/chat/completions` совместимы с клиентами OpenAI (Open WebUI, curl, SDK).
- **RAG Pipeline** — embedding вопроса → поиск в Qdrant → фильтрация по similarity → построение prompt → вызов LLM.
- **Загрузка документов** — REST API для upload/list/delete с асинхронной индексацией.
- **Хранение эмбеддингов** — векторные представления чанков сохраняются в Qdrant с metadata.
- **Поиск по базе знаний** — Top-K retrieval с порогом similarity, настраиваемым через `application.yml`.
- **Генерация ответов через LLM** — Ollama Cloud, модель `glm-4.7`.
- **Потоковая генерация (SSE)** — `stream=true` в `/v1/chat/completions`, формат Server-Sent Events совместим с OpenAI.
- **Поддерживаемые форматы документов** — TXT, Markdown (`.md`, `.markdown`), PDF, DOCX.
- **Кэширование embedding** — одинаковые тексты не пересчитываются повторно.
- **OpenAPI / Swagger UI** — интерактивная документация Document API.
- **Docker Compose** — развёртывание app + Qdrant + Open WebUI (+ SearXNG опционально).
- **Веб-поиск как fallback (опционально)** — LLM может вызвать tool `web_search` через SearXNG, если локального контекста недостаточно. Отключён по умолчанию (`WEBSEARCH_ENABLED=false`).

---

## Используемые технологии

| Компонент | Технология |
| --- | --- |
| Язык | Java 21 |
| Framework | Spring Boot 3.5.15 |
| AI | Spring AI 1.1.8 |
| LLM | Ollama Cloud (`glm-4.7`) |
| Embedding | Ollama Cloud (`nomic-embed-text`) |
| Vector Store | Qdrant (Spring AI Vector Store Qdrant) |
| REST | Spring Web + Spring WebFlux (SSE) |
| Валидация | Spring Validation |
| Mapping | MapStruct 1.6.3 |
| JSON | Jackson |
| Документация API | springdoc-openapi 2.8.9 |
| Парсинг Markdown | commonmark-java 0.24.0 |
| Парсинг PDF | Apache PDFBox 3.0.5 |
| Парсинг DOCX | Apache POI 5.4.1 |
| Логирование | SLF4J + Logback |
| Сборка | Maven (Maven Wrapper) |
| Контейнеризация | Docker, Docker Compose |
| UI | Open WebUI |
| Веб-поиск (опционально) | SearXNG + Spring AI Tool Calling |
| Тестирование | JUnit 5, Mockito, AssertJ, Spring Boot Test, Testcontainers (Qdrant) |

---

## Архитектура

Сервис построен по слоистой архитектуре (Controller → Service → Repository) с разделением ответственности по пакетам: `parser`, `rag`, `embedding`, `vector`, `llm`, `websearch`.

```text
Open WebUI
        │
OpenAI API  (/v1/models, /v1/chat/completions)
        │
Spring Boot Application
        │
        ├── ChatCompletionService ──► RagService ──► LlmService (Ollama Cloud)
        │                      │
        │                      ├── EmbeddingService (nomic-embed-text)
        │                      ├── VectorStoreService ──► Qdrant
        │                      └── PromptBuilderService
        │
        ├── DocumentController ──► DocumentService
        │                      │
        │                      └── DocumentProcessingService (async)
        │                               ├── DocumentParserRegistry
        │                               ├── ChunkingService
        │                               ├── EmbeddingService
        │                               └── VectorStoreService
        │
        └── WebSearchTool (опционально) ──► SearXNG
```

| Компонент | Назначение |
| --- | --- |
| **Open WebUI** | Web-интерфейс для чата; подключается к сервису как к OpenAI API. |
| **OpenAI API layer** | Совместимый REST-слой (`/v1/*`) для внешних клиентов. |
| **Spring Boot** | HTTP-сервер, DI, конфигурация, обработка ошибок. |
| **RAG Pipeline** | Retrieval-Augmented Generation: поиск контекста + генерация ответа. |
| **Qdrant** | Векторная база для хранения и поиска embedding чанков документов. |
| **Ollama Cloud** | LLM (`glm-4.7`) и embedding-модель (`nomic-embed-text`). |
| **Document Processing** | Асинхронный pipeline: парсинг → чанкинг → embedding → индексация. |
| **SearXNG (опционально)** | Метапоисковик; используется LLM через tool calling при нехватке контекста. |

Метаданные документов (имя файла, статус, количество чанков) хранятся in-memory в `InMemoryDocumentMetadataRepository`. Векторные данные и metadata чанков — в Qdrant.

---

## Поддерживаемые форматы документов

| Формат | Расширения | Парсер |
| --- | --- | --- |
| Plain Text | `.txt` | Встроенные средства Java |
| Markdown | `.md`, `.markdown` | commonmark-java |
| PDF | `.pdf` | Apache PDFBox |
| DOCX | `.docx` | Apache POI |

Все парсеры реализуют общий интерфейс `DocumentParser`:

```java
public interface DocumentParser {
    boolean supports(String extension);
    String parse(InputStream stream);
}
```

---

## API

Базовый URL: `http://localhost:8080`

### GET /v1/models

Возвращает список доступных моделей в формате OpenAI API.

**Пример запроса:**

```bash
curl http://localhost:8080/v1/models
```

**Пример ответа (`200 OK`):**

```json
{
  "object": "list",
  "data": [
    {
      "id": "glm-4.7",
      "object": "model",
      "created": 1749254400,
      "owned_by": "ollama"
    }
  ]
}
```

---

### POST /v1/chat/completions

Генерирует ответ на основе RAG pipeline. Использует последнее сообщение с ролью `user` из массива `messages`.

**Поддерживаемые параметры запроса:**

| Параметр | Тип | Обязательный | Описание |
| --- | --- | --- | --- |
| `model` | string | нет | Идентификатор модели. По умолчанию: `glm-4.7`. |
| `messages` | array | да | Массив сообщений `{ "role": "...", "content": "..." }`. |
| `stream` | boolean | нет | `true` — потоковый ответ через SSE; `false` или отсутствует — JSON. |

> **Примечание:** Параметры OpenAI API `temperature`, `max_tokens`, `top_p` и другие в текущей реализации **не поддерживаются** — запрос принимает только `model`, `messages` и `stream`.

**Пример запроса (синхронный):**

```bash
curl -X POST http://localhost:8080/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "glm-4.7",
    "messages": [
      {
        "role": "user",
        "content": "Что содержится в загруженных документах?"
      }
    ],
    "stream": false
  }'
```

**Пример ответа (`200 OK`):**

```json
{
  "id": "chatcmpl-a1b2c3d4e5f6",
  "object": "chat.completion",
  "created": 1749254400,
  "model": "glm-4.7",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "На основе загруженных документов..."
      },
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 0,
    "completion_tokens": 0,
    "total_tokens": 0
  }
}
```

**Пример запроса (потоковый, SSE):**

```bash
curl -N -X POST http://localhost:8080/v1/chat/completions \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{
    "model": "glm-4.7",
    "messages": [
      {
        "role": "user",
        "content": "Расскажи кратко о проекте"
      }
    ],
    "stream": true
  }'
```

**Пример потокового ответа (`200 OK`, `Content-Type: text/event-stream`):**

```text
data: {"id":"chatcmpl-a1b2c3d4e5f6","object":"chat.completion.chunk","created":1749254400,"model":"glm-4.7","choices":[{"index":0,"delta":{"role":"assistant"},"finish_reason":null}]}

data: {"id":"chatcmpl-a1b2c3d4e5f6","object":"chat.completion.chunk","created":1749254400,"model":"glm-4.7","choices":[{"index":0,"delta":{"content":"Ответ"},"finish_reason":null}]}

data: {"id":"chatcmpl-a1b2c3d4e5f6","object":"chat.completion.chunk","created":1749254400,"model":"glm-4.7","choices":[{"index":0,"delta":null,"finish_reason":"stop"}]}

data: [DONE]
```

---

### POST /api/documents/upload

Принимает файл, сохраняет метаданные и запускает **асинхронную** индексацию. Возвращает статус `202 Accepted`.

**Поддерживаемые форматы:** `.txt`, `.md`, `.markdown`, `.pdf`, `.docx`

**Ограничение размера:** 50 MB (`spring.servlet.multipart.max-file-size`)

**Content-Type:** `multipart/form-data`

**Поле формы:** `file`

**Пример запроса:**

```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@./document.pdf"
```

**Пример ответа (`202 Accepted`):**

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "filename": "document.pdf",
  "status": "PROCESSING"
}
```

**Возможные статусы документа:**

| Статус | Описание |
| --- | --- |
| `PROCESSING` | Документ обрабатывается (парсинг, чанкинг, embedding, индексация). |
| `COMPLETED` | Документ успешно проиндексирован. |
| `FAILED` | Ошибка обработки (см. поле `errorMessage` в списке документов). |

---

### GET /api/documents

Возвращает список всех загруженных документов с метаданными.

**Пример запроса:**

```bash
curl http://localhost:8080/api/documents
```

**Пример ответа (`200 OK`):**

```json
{
  "documents": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "filename": "document.pdf",
      "status": "COMPLETED",
      "uploadedAt": "2026-07-07T08:30:00Z",
      "chunkCount": 12,
      "errorMessage": null
    },
    {
      "id": "7b2c9d1e-4f3a-5b6c-8d9e-0f1a2b3c4d5e",
      "filename": "notes.txt",
      "status": "FAILED",
      "uploadedAt": "2026-07-07T08:25:00Z",
      "chunkCount": 0,
      "errorMessage": "Document contains no indexable text"
    }
  ],
  "total": 2
}
```

---

### DELETE /api/documents/{id}

Удаляет документ: метаданные из in-memory хранилища и все связанные чанки из Qdrant.

**Пример запроса:**

```bash
curl -X DELETE http://localhost:8080/api/documents/3fa85f64-5717-4562-b3fc-2c963f66afa6
```

**Пример ответа (`204 No Content`):**

Тело ответа отсутствует.

**Пример ошибки (`404 Not Found`):**

```json
{
  "timestamp": "2026-07-07T08:35:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Document not found: 3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "path": "/api/documents/3fa85f64-5717-4562-b3fc-2c963f66afa6"
}
```

---

## Переменные окружения

| Переменная | Назначение | Значение по умолчанию |
| --- | --- | --- |
| `OLLAMA_API_KEY` | API-ключ Ollama Cloud | — (обязателен для production) |
| `OLLAMA_BASE_URL` | Base URL Ollama API | `https://ollama.com` |
| `QDRANT_HOST` | Хост Qdrant (gRPC) | `localhost` |
| `QDRANT_PORT` | Порт Qdrant gRPC | `6334` |
| `WEBSEARCH_ENABLED` | Включить tool calling для веб-поиска | `false` |
| `SEARXNG_BASE_URL` | Base URL SearXNG | `http://localhost:8081` |
| `SERVER_PORT` | HTTP-порт приложения | `8080` |
| `SPRING_PROFILES_ACTIVE` | Активный Spring-профиль | — |

> API-ключ **не хранится в коде** — передаётся только через переменные окружения или secrets Docker Compose.

---

## Конфигурация

Основной файл: `src/main/resources/application.yml`

```yaml
server:
  port: 8080

spring:
  application:
    name: rag-service
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB
  ai:
    ollama:
      base-url: ${OLLAMA_BASE_URL:https://ollama.com}
      api-key: ${OLLAMA_API_KEY:}
      chat:
        options:
          model: glm-4.7          # LLM-модель
      embedding:
        options:
          model: nomic-embed-text # Embedding-модель
    vectorstore:
      qdrant:
        host: ${QDRANT_HOST:localhost}
        port: ${QDRANT_PORT:6334}
        collection-name: rag-documents
        initialize-schema: true

rag:
  chunk-size: 1000              # Размер чанка (символы)
  overlap: 200                  # Перекрытие между чанками
  top-k: 5                      # Количество чанков при поиске
  similarity-threshold: 0.7     # Минимальный порог similarity
  async:
    core-pool-size: 4
    max-pool-size: 8
    queue-capacity: 100
  web-search:                   # Опционально
    enabled: ${WEBSEARCH_ENABLED:false}
    base-url: ${SEARXNG_BASE_URL:http://localhost:8081}
    max-results: 5

openai:
  api:
    default-model: glm-4.7
    model-owner: ollama

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
```

---

## Запуск

### Локально

**Требования:**

- Java 21+
- Docker (для Qdrant)
- Maven Wrapper (включён в репозиторий) или Maven 3.9+
- API-ключ Ollama Cloud

**1. Клонировать репозиторий:**

```bash
git clone <repository-url>
cd rag
```

**2. Запустить Qdrant:**

```bash
docker compose up -d qdrant
```

**3. Задать переменные окружения:**

```bash
export OLLAMA_API_KEY=your-ollama-api-key
export QDRANT_HOST=localhost
export QDRANT_PORT=6334
```

**4. Запустить приложение:**

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

**5. Проверить доступность:**

```bash
curl http://localhost:8080/v1/models
```

---

### Через Docker Compose

**Базовый стек (app + Qdrant + Open WebUI):**

```bash
export OLLAMA_API_KEY=your-ollama-api-key
docker compose up --build
```

**С опциональным веб-поиском (SearXNG):**

```bash
export OLLAMA_API_KEY=your-ollama-api-key
export WEBSEARCH_ENABLED=true
docker compose --profile websearch up --build
```

**Запускаемые контейнеры:**

| Контейнер | Образ | Порт | Описание |
| --- | --- | --- | --- |
| `app` | Сборка из `Dockerfile` | `8080` | RAG-сервис (Spring Boot) |
| `qdrant` | `qdrant/qdrant:v1.13.4` | `6333`, `6334` | Векторная база данных |
| `open-webui` | `ghcr.io/open-webui/open-webui:main` | `3000` | Web UI для чата |
| `searxng` *(опционально)* | `searxng/searxng:latest` | `8081` | Метапоисковик для tool calling |

---

## Swagger

Интерактивная документация Document API:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON:

```
http://localhost:8080/api-docs
```

> Эндпоинты OpenAI API (`/v1/*`) скрыты из Swagger (`@Hidden`) — они предназначены для совместимости с OpenAI-клиентами.

---

## Open WebUI

Open WebUI подключается к сервису через **OpenAI-совместимый API**.

При запуске через Docker Compose Open WebUI уже настроен:

| Параметр | Значение |
| --- | --- |
| Base URL | `http://app:8080/v1` (внутри Docker-сети) |
| API Key | `rag-local-key` (произвольное значение) |

**Доступ:** http://localhost:3000

**Ручная настройка (без Docker Compose):**

1. Открыть Open WebUI → Settings → Connections.
2. Добавить OpenAI-compatible connection.
3. Указать:
   - **API Base URL:** `http://localhost:8080/v1`
   - **API Key:** любое значение (сервис не проверяет ключ на стороне приложения).
4. Выбрать модель `glm-4.7`.

---

## Примеры использования

### Получение списка моделей

```bash
curl http://localhost:8080/v1/models
```

### Генерация ответа (RAG)

```bash
curl -X POST http://localhost:8080/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "model": "glm-4.7",
    "messages": [
      {"role": "user", "content": "Какие темы описаны в документах?"}
    ]
  }'
```

### Потоковая генерация

```bash
curl -N -X POST http://localhost:8080/v1/chat/completions \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{
    "model": "glm-4.7",
    "messages": [
      {"role": "user", "content": "Кратко опиши содержимое базы знаний"}
    ],
    "stream": true
  }'
```

### Загрузка документа

```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@./report.pdf"
```

### Просмотр документов

```bash
curl http://localhost:8080/api/documents
```

### Удаление документа

```bash
curl -X DELETE http://localhost:8080/api/documents/3fa85f64-5717-4562-b3fc-2c963f66afa6
```

---

## Процесс обработки документа

После загрузки файла через `/api/documents/upload` запускается асинхронный pipeline (`CompletableFuture` + выделенный `ThreadPoolTaskExecutor`):

```text
Upload (HTTP 202 Accepted)
        │
        ▼
Parsing (DocumentParserRegistry → TXT / MD / PDF / DOCX)
        │
        ▼
Chunking (ChunkingService: chunk-size, overlap)
        │
        ▼
Embedding (EmbeddingService + кэш)
        │
        ▼
Qdrant (VectorStoreService: vectors + metadata)
        │
        ▼
Ready (status: COMPLETED)
```

**Metadata каждого чанка в Qdrant:**

| Ключ | Описание |
| --- | --- |
| `documentId` | UUID документа |
| `filename` | Имя файла |
| `chunkIndex` | Номер чанка (zero-based) |
| `chunkText` | Текст чанка |
| `uploadedAt` | Timestamp загрузки (ISO-8601) |

---

## RAG Pipeline

При каждом запросе к `/v1/chat/completions` выполняется:

```text
Question (последнее user-сообщение)
        │
        ▼
Embedding (EmbeddingService → nomic-embed-text)
        │
        ▼
Vector Search (Qdrant: top-k + similarity-threshold)
        │
        ▼
Context (форматирование найденных чанков с указанием источника)
        │
        ▼
Prompt Builder (PromptBuilderService + шаблон)
        │
        ▼
LLM (Ollama Cloud → glm-4.7)
        │
        ▼
Response (JSON или SSE stream)
```

**Шаблон prompt:**

```text
Ты — помощник, отвечающий исключительно по предоставленному контексту.

Если информации недостаточно — честно сообщи об этом.

Не выдумывай факты.

Если возможно — указывай источник.

Контекст:

{context}

Вопрос:

{question}
```

При включённом веб-поиске (`WEBSEARCH_ENABLED=true`) LLM дополнительно может вызвать tool `web_search` для получения актуальной информации из SearXNG.

---

## Структура проекта

```text
src/main/java/com/rag/
├── RagServiceApplication.java
├── config/
│   ├── AppConfig.java
│   ├── AsyncConfig.java
│   ├── LlmConfig.java
│   ├── OpenAiApiProperties.java
│   ├── RagProperties.java
│   └── WebSearchProperties.java
├── controller/
│   ├── ChatController.java          # /v1/models, /v1/chat/completions
│   └── DocumentController.java      # /api/documents/*
├── service/
│   ├── ChatCompletionService.java
│   ├── DocumentService.java
│   └── DocumentProcessingService.java
├── repository/
│   ├── DocumentMetadataRepository.java
│   └── InMemoryDocumentMetadataRepository.java
├── dto/
│   ├── openai/                      # OpenAI-compatible DTO
│   ├── DocumentListResponse.java
│   ├── DocumentSummaryResponse.java
│   ├── DocumentUploadResponse.java
│   ├── ErrorResponse.java
│   └── FieldErrorDetail.java
├── entity/
│   ├── DocumentMetadata.java
│   └── DocumentStatus.java
├── mapper/
│   └── DocumentMapper.java
├── parser/
│   ├── DocumentParser.java
│   ├── DocumentParserRegistry.java
│   ├── TxtDocumentParser.java
│   ├── MarkdownDocumentParser.java
│   ├── PdfDocumentParser.java
│   └── DocxDocumentParser.java
├── rag/
│   ├── RagService.java
│   ├── ChunkingService.java
│   └── PromptBuilderService.java
├── embedding/
│   └── EmbeddingService.java
├── vector/
│   ├── VectorStoreService.java
│   └── VectorMetadataKeys.java
├── llm/
│   └── LlmService.java
├── websearch/                       # Опционально
│   ├── WebSearchService.java
│   ├── SearXngWebSearchService.java
│   └── WebSearchTool.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── RagException.java
│   ├── LlmException.java
│   ├── DocumentNotFoundException.java
│   ├── DocumentProcessingException.java
│   └── UnsupportedFileTypeException.java
└── util/
    ├── FileExtensionUtils.java
    └── OpenAiResponseUtils.java

src/main/resources/
└── application.yml

src/test/java/com/rag/
├── embedding/
├── exception/
├── parser/
├── rag/
├── util/
├── vector/                          # Testcontainers + Qdrant
└── websearch/

docker/
└── searxng/
    └── settings.yml

Dockerfile
docker-compose.yml
pom.xml
```

---

## Дорожная карта

### Реализовано

- [x] OpenAI-compatible API (`GET /v1/models`, `POST /v1/chat/completions`)
- [x] Потоковая генерация через SSE (`stream=true`)
- [x] RAG Pipeline (embedding → vector search → prompt → LLM)
- [x] Document API (upload / list / delete)
- [x] Асинхронная обработка документов (`CompletableFuture` + `ThreadPoolTaskExecutor`)
- [x] Парсеры: TXT, Markdown, PDF, DOCX
- [x] ChunkingService с настройками из `application.yml`
- [x] EmbeddingService с кэшированием
- [x] Хранение векторов и metadata в Qdrant
- [x] Единый формат ошибок (`@RestControllerAdvice`)
- [x] OpenAPI / Swagger UI
- [x] Docker + Docker Compose (app, Qdrant, Open WebUI)
- [x] Интеграционные тесты Qdrant (Testcontainers)
- [x] Веб-поиск через SearXNG + Spring AI Tool Calling *(опционально, отключён по умолчанию)*

### Планируется

- [ ] Персистентное хранение метаданных документов (сейчас in-memory; данные теряются при перезапуске)
- [ ] Поддержка дополнительных параметров OpenAI API (`temperature`, `max_tokens`, `top_p`)
- [ ] Spring Boot Actuator (health checks, metrics) для production-мониторинга

---


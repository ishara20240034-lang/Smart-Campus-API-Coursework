# Smart Campus Sensor & Room Management API

**Module:** 5COSC022W — Client-Server Architectures (2025/26)  
**Title:** Smart Campus RESTful Web Service  
**Technology:** JAX-RS (Jakarta RESTful Web Services) on GlassFish 7  

---

## API Design Overview

This backend service models the physical infrastructure of a university "Smart Campus". It exposes a versioned RESTful API (`/api/v1`) to allow facilities managers and automated building systems to manage Rooms, Sensors, and historical Sensor Readings.

**Key architectural decisions:**

- **Thread Safety** is enforced throughout by using `ConcurrentHashMap` for all in-memory data stores, preventing race conditions in a multi-threaded server environment.
- **Resource Nesting** follows a strict hierarchical model: a Sensor belongs to a Room, and Readings belong to a Sensor. This is reflected in the URL structure (`/sensors/{id}/readings`).
- **Sub-Resource Locator Pattern** is used to delegate reading management to a dedicated `SensorReadingResource` class, keeping controllers focused and maintainable.
- **Custom Exception Mapping Framework** ensures the API is "leak-proof" — no raw Java stack traces are ever exposed. All error scenarios return structured JSON bodies with semantically correct HTTP status codes (409, 422, 403, 500).
- **Bidirectional Linking** is maintained between Rooms and Sensors. When a Sensor is registered, its ID is added to the parent Room's `sensorIds` list, enabling the orphan-protection constraint on room deletion.

---

## How to Build and Run

### Prerequisites

- Java Development Kit (JDK) 11 or higher
- Apache NetBeans IDE (recommended)
- GlassFish Server 7 configured within NetBeans

### Step-by-Step Instructions

1. **Clone the Repository:** Download or clone this project to your local machine.
2. **Open in NetBeans:** Go to `File` → `Open Project` and select the `SmartCampusAPI` folder.
3. **Clean and Build:** Click the **Clean and Build** button (hammer and broom icon) in the toolbar. Wait for `BUILD SUCCESS` in the output window — this downloads Maven dependencies and compiles all classes.
4. **Deploy:** Right-click the `SmartCampusAPI` project in the Projects panel and select **Run**.
5. **Verify:** The API will be live at: [click here] (http://localhost:8080/SmartCampusAPI/api/v1)

---

## API Endpoint Reference

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1` | Discovery — returns API metadata and resource map |
| GET | `/api/v1/rooms` | Retrieve all rooms |
| POST | `/api/v1/rooms` | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | Retrieve a specific room by ID |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (blocked if sensors are assigned) |
| GET | `/api/v1/sensors` | Retrieve all sensors (supports `?type=` filter) |
| POST | `/api/v1/sensors` | Register a new sensor (validates room existence) |
| GET | `/api/v1/sensors/{sensorId}` | Retrieve a specific sensor by ID |
| GET | `/api/v1/sensors/{sensorId}/readings` | Retrieve historical readings for a sensor |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a new reading (blocked if sensor is OFFLINE/MAINTENANCE) |

---

## Sample cURL Commands

### 1. Discovery Endpoint
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1 \
  -H "Accept: application/json"
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id": "LIB-301", "name": "Library Quiet Study", "capacity": 50}'
```

### 3. Register a Sensor (linked to the room above)
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "TEMP-001", "type": "Temperature", "status": "ACTIVE", "currentValue": 22.5, "roomId": "LIB-301"}'
```

### 4. Filter Sensors by Type
```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=Temperature" \
  -H "Accept: application/json"
```

### 5. Add a Historical Reading
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 28.9}'
```

### 6. Retrieve Historical Readings
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Accept: application/json"
```

### 7. Trigger a 409 Conflict (delete occupied room)
```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301 \
  -H "Accept: application/json"
```

### 8. Trigger a 422 Unprocessable Entity (register sensor with invalid roomId)
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "CO2-999", "type": "CO2", "status": "ACTIVE", "currentValue": 400.0, "roomId": "NONEXISTENT"}'
```

---

## Conceptual Report

### Part 1 — Service Architecture & Setup

**Q: Explain the default lifecycle of a JAX-RS Resource class. How does this impact in-memory data management?**

By default, JAX-RS resource classes are **request-scoped**: the runtime instantiates a new object for every incoming HTTP request, and that object is discarded once the response is sent. This means any instance-level fields (ordinary variables declared on the class) are created fresh per request and immediately lost — they cannot hold persistent state across calls.

To retain data between requests, the data stores in this application are declared as `static` fields on the resource classes. A `static` field belongs to the class itself, not to any individual instance, so it persists in memory for the entire lifetime of the deployed application, surviving across the new instances created per request.

However, since GlassFish is a multi-threaded server and may process several requests concurrently, a standard `HashMap` would be vulnerable to race conditions (e.g., two threads simultaneously writing to the same map could corrupt its internal structure). This is solved by using `ConcurrentHashMap`, which provides thread-safe operations without requiring explicit `synchronized` blocks, preventing data loss or corruption under concurrent load.

---

**Q: Why is HATEOAS considered a hallmark of advanced RESTful design?**

HATEOAS (Hypermedia as the Engine of Application State) means that API responses include navigational links that tell the client what actions are available next, rather than the client needing to guess or consult external documentation. For example, a response to `GET /rooms/LIB-301` could include a `_links` object pointing to its sensors at `/api/v1/rooms/LIB-301/sensors`.

This benefits client developers significantly: instead of studying a static PDF to understand which URLs to call, the client application can dynamically discover available operations from the responses themselves. This makes the API self-documenting, reduces coupling between the client and the server's URL structure, and means that if the server changes a URL path, compliant clients that follow links rather than hardcoding paths will continue to work. This is why HATEOAS is regarded as the highest maturity level of RESTful API design (Level 3 of the Richardson Maturity Model).

---

### Part 2 — Room Management

**Q: What are the implications of returning only IDs versus full room objects in a list response?**

Returning only IDs is bandwidth-efficient and results in smaller, faster initial responses. However, it forces the client to make a separate `GET /rooms/{id}` request for every ID in the list to retrieve usable data — an "N+1 request problem" that multiplies network round-trips and degrades client-side rendering performance.

Returning full room objects requires more bandwidth in the initial response, but eliminates all follow-up requests. For most campus dashboard scenarios where a manager needs to display the full list of rooms immediately, the single larger payload is a better trade-off than N+1 sequential requests. The optimal design depends on context: ID-only suits scenarios where only a subset will be accessed, while full objects suit scenarios where the entire collection is needed at once.

---

**Q: Is the DELETE operation idempotent in your implementation?**

Yes, DELETE is idempotent in this implementation. Idempotency means that making the same request multiple times produces the same server state as making it once.

- **First DELETE** on `/rooms/LIB-301` (assuming no sensors): the room is removed from the data store and the server returns `204 No Content`.
- **Second DELETE** on the same path: the room no longer exists, so `roomDatabase.get(roomId)` returns `null`. The service returns `404 Not Found`.

In both cases, the server-side state after the call is identical — the room does not exist. The response code differs (204 vs 404), but the state of the resource is unchanged by the second call, satisfying the idempotency requirement. This also provides meaningful feedback to the client, confirming why the operation produced no effect.

---

### Part 3 — Sensor Operations & Linking

**Q: What are the technical consequences if a client sends data in a format other than `application/json` to a `@Consumes(MediaType.APPLICATION_JSON)` endpoint?**

The `@Consumes` annotation instructs the JAX-RS runtime to only route requests to that method if the incoming `Content-Type` header matches `application/json`. If a client sends a request with `Content-Type: text/plain` or `Content-Type: application/xml`, the runtime intercepts the mismatch before the Java method is ever executed and automatically returns an **HTTP 415 Unsupported Media Type** response. This is a declarative content negotiation mechanism — the developer does not need to write any manual type-checking logic inside the method body.

---

**Q: Why is the `@QueryParam` approach superior to embedding the filter in the URL path (e.g., `/sensors/type/CO2`)?**

A URL path segment like `/sensors/type/CO2` implies that `type/CO2` is a distinct, addressable resource with a fixed identity — which is semantically incorrect for a filter. Query parameters (`?type=CO2`) are the correct REST convention for filtering, searching, or sorting a collection because they are **optional**, can be **combined** (e.g., `?type=CO2&status=ACTIVE`), and do not alter the fundamental resource hierarchy. The base collection at `/api/v1/sensors` remains clean and addressable, while the query string simply narrows what is returned. Path-based filtering also creates a maintenance burden, requiring new routes for every filter combination.

---

### Part 4 — Sub-Resource Locator Pattern

**Q: What are the architectural benefits of the Sub-Resource Locator pattern?**

The Sub-Resource Locator pattern, implemented via a method in `SensorResource` that returns a `SensorReadingResource` instance, provides several architectural advantages over defining all nested paths in a single controller class.

First, it enforces the **Single Responsibility Principle**: `SensorResource` is only responsible for sensor-level operations, while `SensorReadingResource` handles reading-level operations. This prevents any single class from becoming an unmanageable monolith as the API grows.

Second, it improves **testability**: each resource class can be unit tested in isolation without needing to instantiate the entire controller hierarchy.

Third, it improves **maintainability**: a developer working on reading history logic can modify `SensorReadingResource` without touching or risking regressions in `SensorResource`. In a large campus API with dozens of nested resources, this modularity is essential for parallel team development and long-term code quality.

---

### Part 5 — Error Handling & Logging

**Q: Why is HTTP 422 Unprocessable Entity more semantically accurate than 404 Not Found when a `roomId` reference in the payload doesn't exist?**

The distinction lies in precisely what has failed. A **404 Not Found** response means the requested endpoint URL itself does not exist on the server — the route was not found. This would be the correct response if a client called `/api/v1/nonexistentpath`.

In this scenario, however, the client posted a valid JSON payload to a valid, existing endpoint (`POST /api/v1/sensors`). The URL was found, the `Content-Type` was correct, and the JSON was syntactically well-formed and successfully parsed. The failure is semantic — the `roomId` value inside the payload references an entity that does not exist in the system. The request was understood by the server but cannot be processed due to a logical constraint in the content.

**HTTP 422 Unprocessable Entity** maps precisely to this situation: "I understood your request, but I cannot act on it because the payload contains a reference that fails a business rule." Returning 404 in this context would mislead the client into thinking the endpoint itself doesn't exist, causing incorrect debugging behaviour. A 422 communicates the exact failure layer (content semantics, not routing), giving the client developer actionable and accurate feedback.

---

**Q: From a cybersecurity standpoint, what are the risks of exposing Java stack traces to external API consumers?**

Exposing raw stack traces to external clients represents a serious **information disclosure vulnerability** with multiple attack vectors:

1. **Library and version fingerprinting:** A stack trace typically includes fully-qualified class names from third-party dependencies (e.g., `org.glassfish.jersey.server.ServerRuntime` or `com.fasterxml.jackson.databind.ObjectMapper`). An attacker can cross-reference these exact library names and version numbers against public CVE (Common Vulnerabilities and Exposures) databases to identify known, unpatched security vulnerabilities in the specific version of the framework being used, then craft targeted exploits.

2. **Internal architecture mapping:** Stack traces reveal the internal package structure, class naming conventions, and file paths of the application (e.g., `com.smartcampus.resources.SensorResource.createSensor(SensorResource.java:47)`). This allows an attacker to build an accurate mental model of the codebase's architecture without access to the source code.

3. **Logic flaw identification:** The sequence of method calls visible in a trace reveals business logic flow — which methods call which, what validation order is used, and where exceptions are thrown. This can expose logic flaws or bypasses that an attacker can exploit.

The `GlobalExceptionMapper<Throwable>` in this application addresses this by catching all unhandled runtime exceptions and returning a generic, non-technical `500 Internal Server Error` message. The full exception is logged server-side (visible only to administrators), while the client receives no exploitable information.

---

**Q: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging?**

Using a `ContainerRequestFilter` / `ContainerResponseFilter` for logging follows the **DRY (Don't Repeat Yourself)** principle and the concept of **Aspect-Oriented Programming**. Cross-cutting concerns are behaviours that apply uniformly across all endpoints regardless of their business logic.

Without filters, a developer would need to manually add `Logger.info()` statements to every single resource method — duplicating boilerplate across potentially dozens of classes. This creates maintenance overhead (forgetting to add logging to a new endpoint means blind spots in observability), increases the risk of inconsistency (different methods logging in different formats), and pollutes business logic with infrastructure concerns.

A single filter class applies uniformly to every request and response automatically, with zero modification to the resource classes themselves. This produces consistent, complete observability across the entire API surface from one central location.

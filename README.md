# Smart Campus Infrastructure API

## 1. Project Overview
This repository contains a backend RESTful service developed to manage university resources such as rooms and environmental sensors. The application is built using Jakarta RESTful Web Services (JAX-RS) and is hosted on a lightweight Grizzly server. It supports standard CRUD operations and handles historical sensor telemetry.

Key Architectural Features:
* *Stateless Operations*: Fully compliant with REST constraints, utilizing standard HTTP verbs and status codes for clear communication.
* *In-Memory Datastore*: Employs ConcurrentHashMap to maintain state across request-scoped JAX-RS resources safely.
* *Hierarchical Routing*: Uses sub-resource locators to neatly separate logic for deeply nested endpoints (like sensor readings).
* *Robust Error Management*: Custom ExceptionMappers are implemented to catch business rule violations and return clean JSON error messages without exposing sensitive stack traces.
* *HATEOAS Navigation*: The root endpoint provides hypermedia links, allowing dynamic client navigation.

## 2. Setup and Execution Instructions
To run this application locally:
1. Launch NetBeans IDE.
2. Select "Open Project" and choose the SmartCampus-1.0-SNAPSHOT project folder.
3. Allow Maven to download all required dependencies.
4. In the "Source Packages" folder, navigate to the com.mycompany.smartcampus package.
5. Locate Main.java, right-click on it, and select "Run File".
6. The server will boot up and listen for requests (typically at http://localhost:8080/api/v1).

## 3. API Endpoints Reference
You can test the API using Postman or cURL. Below are the available operations:

### Base Discovery
* GET /api/v1 - Retrieves API metadata and navigation links.

### Room Endpoints
* POST /api/v1/rooms - Registers a new room. (Body: {"id": "Lec-A", "name": "Main Hall", "capacity": 150})
* GET /api/v1/rooms - Retrieves all registered rooms.
* GET /api/v1/rooms/{id} - Retrieves details for a specific room.
* DELETE /api/v1/rooms/{id} - Removes a room. Will return a *409 Conflict* if sensors are currently assigned to it.

### Sensor Endpoints
* POST /api/v1/sensors - Registers a new sensor. (Body: {"id": "S-01", "type": "CO2", "status": "ACTIVE", "roomId": "Lec-A"}). Returns *422 Unprocessable Entity* if the room ID is invalid.
* GET /api/v1/sensors?type={type} - Retrieves all sensors, optionally filtered by the provided type.

### Sensor Readings
* POST /api/v1/sensors/{sensorId}/readings - Records a new reading. (Body: {"id": "R-100", "timestamp": 1600000000, "value": 450.5}). Returns *403 Forbidden* if the sensor is in MAINTENANCE mode.
* GET /api/v1/sensors/{sensorId}/readings - Retrieves the historical telemetry data for the specified sensor.

## 4. Coursework Theoretical Answers

### Part 1: Service Architecture & Setup 

*Q:In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race con ditions
*A:  The default scope of JAX-RS resources is request based. This implies that a new object of the resource is created with each incoming HTTP request. Due to this lifecycle, you cannot save persistent data in the form of instance variables in the resource itself. In-memory data structures must be handled by external thread-safe collections to avoid losing data, and to avoid race conditions when multiple requests arrive at the server at the same time.

*Q:Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?
*A: HATEOAS turns an API into a discoverable state machine, rather than a fixed set of endpoints. The API informs the client of what can be done by directly including hypermedia links in the JSON responses. This greatly decreases the dependency of the client on hardcoded URLs and out-of-band documentation and results in a system that is far more malleable to changes in back-end routing.

### Part 2: Room Management

*Q: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing
*A:Sending only IDs will decrease the size of the initial network payload, but will cause the client to issue additional follow-up requests to retrieve the real information about each room, which will put a strain on the server load and latency. On the other hand, full room object delivery will cause the starting bandwidth to be higher, but the client-side application can present a full dashboard after one request, which is usually more efficient to process.

*Q: IstheDELETEoperationidempotentinyourimplementation? Provideadetailed justification by describingwhathappensifaclientmistakenlysendstheexactsameDELETE request for a room multiple time.
*A:Yes, the method of implementation makes sure that the DELETE method is completely idempotent. When a user makes a DELETE request towards a particular room, it is deleted and a 204 status code is returned. In case they make an accidental repeat request with the same request, the server will respond with a 404 Not Found. Most importantly, the outcome on the state of the server will be the same no matter the number of times the request was replicated: the resource has been deleted.

### Part 3: Sensor Operations & Linking

*Q:We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?
*A:Because the POST method is strictly annotated with @Consumes(MediaType.APPLICATION_JSON) , if a client attempts to send data in a format like text/plain or application/xml, the request is intercepted by the JAX-RS runtime before it even reaches the resource method. JAX-RS automatically handles this mismatch by rejecting the request and returning an HTTP 415 Unsupported Media Type error.

*Q:: Youimplementedthisfilteringusing@QueryParam. Contrastthiswithanalterna tive design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the queryparameterapproachgenerallyconsideredsuperiorforfilteringandsearching collections?
*A:Filtering is better suited to query parameters, which are optional by nature, stackable and leave the underlying identity of the resource collection unchanged. Path parameters would make the API strictly hierarchical, and it would be un-scalable and difficult to re-use with clients requiring to use several dynamic filters at the same time.

### Part 4: Deep Nesting with Sub- Resources

*Q:Discuss the architectural benefits of the Sub-Resource Locator pattern. How doesdelegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive con troller class?
*A:In cases where APIs contain highly-nested endpoints, it becomes extremely cumbersome to house all these routes inside the single parent controller, resulting in huge non-readable classes. The Sub-Resource Locator pattern addresses this by passing the nested request to a special sub-class. This imposes the Single Responsibility Principle which makes controllers small, focused, and far easier to maintain.

### Part 5: Advanced Error Handling, Exception Mapping & Logging

*Q:WhyisHTTP422oftenconsideredmoresemanticallyaccurate than a standard 404 whenthe issue is a missing reference inside a valid JSON payload?
*A:The HTTP 422 Unprocessable Entity is syntactically correct but fails a business rule Logical An entity that is presented by the client in a perfectly-formatted and syntactically-valid JSON payload and fails a logical business rule, such as a roomId that does not exist in the system. The 404 Not Found standard generally assumes that something wrong is with the URL of the endpoint to the API, and this would be misleading to the customer in this case.

*Q:From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?
*A:Publication of raw stack traces is a serious vulnerability. These logs include detailed inner workings of the application, such as the exact versions of the libraries in use, paths on the host server, and the understanding of the logical flow of the code. Such reconnaissance information can be used by malicious actors to determine known exploits against these versions of libraries or map the architecture of the backend to attack a particular target.

*Q:Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single re source method?
*A:The laborious process of manually pasting logging statements into each method duplicates code and obscures the business logic. JAX-RS filters offer a graceful, centralized approach to these cross-cutting issues. Using a global filter, the server will automatically capture and record all the incoming and outgoing traffic at a single point, making full observability possible without the developers having to remember to add logs to new endpoints.


## 5. Required cURL Commands

*Discovery:*
curl -X GET http://localhost:8080/api/v1

*Register Room:*
curl -X POST http://localhost:8080/api/v1/rooms -H "Content-Type: application/json" -d "{\"id\": \"A-10\", \"name\": \"Computer Lab\", \"capacity\": 40}"

*Register Sensor:*
curl -X POST http://localhost:8080/api/v1/sensors -H "Content-Type: application/json" -d "{\"id\": \"S-99\", \"type\": \"Occupancy\", \"status\": \"ACTIVE\", \"roomId\": \"A-10\"}"

*Record Telemetry Data:*
curl -X POST http://localhost:8080/api/v1/sensors/S-99/readings -H "Content-Type: application/json" -d "{\"id\": \"RD-5\", \"timestamp\": 1713889000, \"value\": 1}"

*Filter Sensors:*
curl -X GET "http://localhost:8080/api/v1/sensors?type=Occupancy"

## 6. Technical Stack
* *Core Language:* Java Standard Edition
* *REST Implementation:* Jersey (JAX-RS)
* *Web Container:* Grizzly HTTP Server
* *Dependency Management:* Apache Maven
* *Serialization:* JSON via Jackson library

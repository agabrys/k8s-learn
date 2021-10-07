# Counter Service

The counter service is a simple stateless service which provides the following API:

| End Point | Description | HTTP Method | Returned Value |
| --------- | ----------- | ----------- | -------------- |
| `/counter/value` | Returns the stored value | GET | integer |
| `/counter/increment` | Increment the stored value by one | GET | N/A |
| `/counter/decrement` | Decrement the stored value by one. The minimal stored value is 0 | GET | N/A |

## Run without Docker

To start the service without using Docker, execute

```shell
.\gradlew bootRun
```

The application is available at `http://localhost:8080` (example `http://localhost:8080/counter/value`).

## Run with Docker

To build a Docker image execute

```shell
.\gradlew dockerBuildImage
```

and next run a new Docker container

```shell
docker run -p 8080:8080 com.github.agabrys/counter-service:dev
```

The application is available at `http://localhost:8080` (example `http://localhost:8080/counter/value`).

Files used to build the image (example `Dockerfile`) are created by the [Gradle Docker](https://bmuschko.github.io/gradle-docker-plugin/current/user-guide/) plugin.

# Counter Service

The counter service is a simple stateless service which provides the following API:

| End Point | Description | HTTP Method | Returned Value |
| --------- | ----------- | ----------- | -------------- |
| `/counter/value` | Returns the stored value | GET | integer |
| `/counter/increment` | Increment the stored value by one | GET | N/A |
| `/counter/decrement` | Decrement the stored value by one. The minimal stored value is 0 | GET | N/A |

## How to Build

To build the project execute

```shell
.\gradlew build
```

The built application is stored in the `build/libs` directory.

## How to Test

To test the project execute

```shell
.\gradlew bootRun
```

The application is available at `http://localhost:8080` (example `http://localhost:8080/counter/value`).

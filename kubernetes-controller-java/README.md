# Kubernetes Contoller

The controller reconciles on the Deployments created with the `${env:LABEL_NAME}=${env:LABEL_VALUE}` label in any namespace and sends a request to the [Counter service](../counter-service-java/) (pointed by `${env:COUNTER_SERVICE_URL}`) to count those deployments.

## How to Build

To build a Docker image execute

```shell
.\gradlew dockerBuildImage
```

Files used to build the image (example `Dockerfile`) are created by the [Gradle Docker](https://bmuschko.github.io/gradle-docker-plugin/current/user-guide/) plugin.

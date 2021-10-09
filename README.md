# K8s Learn

Demo presents how to deploy a Spring Boot application in the local Kubernetes cluster.

## Requirements

* Java 17 (e.g. [SAP Machine](https://sapmachine.io/))
* [Docker](https://docs.docker.com/get-docker/)
* [k3d](https://k3d.io/)

## Launch

1. Launch Docker deamon
2. Create a local registry
   ```shell
   k3d registry create registry.localhost --port 5000
   ```
3. Build the Counter service image
   ```shell
   .\counter-service-java\gradlew dockerBuildImage -p counter-service-java
   ```
4. Push the Counter service image to the local registry
   ```shell
   docker tag com.github.agabrys/counter-service:dev localhost:5000/com.github.agabrys/counter-service:dev
   ```
   ```shell
   docker push localhost:5000/com.github.agabrys/counter-service:dev
   ```
5. Create a local Kubernetes cluster with access to the local registry
   ```shell
   k3d cluster create -p "9080:80@loadbalancer" k8s-learn --registry-use k3d-registry.localhost:5000
   ```
6. Apply Kubernetes configuration
   ```shell
   kubectl apply -f ./deployment.yaml
   ```
7. The Counter service endpoints:
   - `http://localhost:9080/counter/value`
   - `http://localhost:9080/counter/increment`
   - `http://localhost:9080/counter/decrement`

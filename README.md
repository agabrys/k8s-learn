# K8s Learn

Demo presents how to deploy a Spring Boot application in the local Kubernetes cluster.

## Requirements

* Java 17 (e.g. [SAP Machine](https://sapmachine.io/))
* [Docker](https://docs.docker.com/get-docker/)
* [Kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl)
* [k3d](https://k3d.io/)

## Launch

1. Launch Docker deamon
2. Create a local registry
   ```shell
   k3d registry create registry.localhost --port 5000
   ```
3. Build the Counter service and push it to the local registry
   ```shell
   .\counter-service-java\gradlew dockerBuildImage -p counter-service-java
   ```
   ```shell
   docker tag com.github.agabrys/counter-service:latest localhost:5000/com.github.agabrys/counter-service:latest
   ```
   ```shell
   docker push localhost:5000/com.github.agabrys/counter-service:latest
   ```
4. Build the Kubernetes controller and push it to the local registry
   ```shell
   .\kubernetes-controller-java\gradlew dockerBuildImage -p kubernetes-controller-java
   ```
   ```shell
   docker tag com.github.agabrys/kubernetes-controller:latest localhost:5000/com.github.agabrys/kubernetes-controller:latest
   ```
   ```shell
   docker push localhost:5000/com.github.agabrys/kubernetes-controller:latest
   ```
5. Create a local Kubernetes cluster with access to the local registry
   ```shell
   k3d cluster create -p "9080:80@loadbalancer" k8s-learn --registry-use k3d-registry.localhost:5000
   ```
6. Configure Kubernetes configuration
   ```shell
   kubectl apply -f ./counter-service.yaml
   ```
   ```shell
   kubectl apply -f ./kubernetes-controller.yaml
   ```
7. The Counter service endpoints:
   - `http://localhost:9080/counter/value`
   - `http://localhost:9080/counter/increment`
   - `http://localhost:9080/counter/decrement`

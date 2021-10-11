# K8s Learn

Demo presents how to deploy a Spring Boot application in the local Kubernetes cluster.

## Requirements

* Java 17 (e.g. [SAP Machine](https://sapmachine.io/))
* [Docker](https://docs.docker.com/get-docker/)
* [Kubectl](https://kubernetes.io/docs/tasks/tools/#kubectl)
* [k3d](https://k3d.io/)

## Prepare Cluster

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
   The Counter service is configured to returns a number of deployments with the `app=nginx` label

The cluster needs a few seconds (30-60) to provision all pods, so please wait a moment before you start going through the demo steps.

## Demo

Use the following steps to verify that the integration between the Kubernetes controller and the Counter service works properly.

1. Open the Counter service endpoints: `http://localhost:9080/counter/value`. It should return `0`
2. Add the `nginx1` deployment (with `app=nginx`)
   ```shell
   kubectl apply -f ./test-1.yaml
   ```
   The Counter service should return `1`
3. Add the `none-nginx` deployment (without `app=nginx`)
   ```shell
   kubectl apply -f ./test-2.yaml
   ```
   The Counter service should return `1`
4. Add the `nginx2` and `nginx3` deployments (with `app=nginx`)
   ```shell
   kubectl apply -f ./test-3.yaml
   ```
   The Counter service should return `3`
5. Remove the `nginx2` deployement
   ```shell
   kubectl delete deployment nginx2
   ```
   The Counter service should return `2`
6. Remove the `none-nginx` deployment
   ```shell
   kubectl delete deployment none-nginx
   ```
   The Counter service should return `2`

## Notes

The project has been created in two days and almost all used technologies were new to me. I see a lot of fields to improve, but I didn't have enough time to work on it. Some examples:
- create a K8s namespace and use it instead of `default`
- use `fieldSelector` to gather less data when `deployments` are requested
- add tests to the K8s controller written by using [kubernetes-client/java](https://github.com/kubernetes-client/java)
- use more precise resource limits 

The git commits are not descriptive. The main goal was to learn K8s, so I didn't focus on them too much (especially that nobody will read them 😉). Some examples of git messages which I usually write:
- https://github.com/jenkinsci/branch-api-plugin/pull/244/commits/0ba29aae1fbe421f9fc0b46bf50d14889da1233f
- https://github.com/jenkinsci/active-choices-plugin/pull/32/commits/04cf5ec8d39d63c907879cb91bcd498fe5d837a7

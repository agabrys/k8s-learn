package com.github.agabrys.k8s.learn.k8scontroller;

import io.kubernetes.client.extended.controller.builder.ControllerBuilder;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.util.Config;
import java.io.IOException;

// TODO: add tests
public class DeploymentWithLabelController {

  public static void main(String[] args) throws IOException, ApiException {
    var client = Config.defaultClient();
    Configuration.setDefaultApiClient(client);
    var appsV1Api = new AppsV1Api(client);

    var informerFactory = new SharedInformerFactory();
    var indexInformer = informerFactory.sharedIndexInformerFor(
      params -> appsV1Api.listDeploymentForAllNamespacesCall(
        null,
        null,
        null,
        null,
        null,
        "false",
        params.resourceVersion,
        null,
        params.timeoutSeconds,
        params.watch,
        null
      ),
      V1Deployment.class,
      V1DeploymentList.class
    );
    informerFactory.startAllRegisteredInformers();

    var labelChecker = new LabelChecker(System.getenv("LABEL_NAME"), System.getenv("LABEL_VALUE"));
    var counterService = new CounterService(System.getenv("COUNTER_SERVICE_URL"));
    var controller = ControllerBuilder.defaultBuilder(informerFactory)
      .watch(workQueue ->
        ControllerBuilder.controllerWatchBuilder(V1Deployment.class, workQueue)
          .withOnAddFilter(deployment -> labelChecker.hasLabel(deployment))
          .withOnUpdateFilter((oldDeployment, newDeployment) ->
            labelChecker.hasLabel(oldDeployment) != labelChecker.hasLabel(newDeployment)
          )
          .withOnDeleteFilter((deployment, stateUnknown) -> labelChecker.hasLabel(deployment))
          .build()
      )
      .withReconciler(new DeploymentWithLabelReconciler(indexInformer, labelChecker, counterService))
      .withReadyFunc(indexInformer::hasSynced)
      .build();

    ControllerBuilder.controllerManagerBuilder(informerFactory)
      .addController(controller)
      .build()
      .run();
  }
}

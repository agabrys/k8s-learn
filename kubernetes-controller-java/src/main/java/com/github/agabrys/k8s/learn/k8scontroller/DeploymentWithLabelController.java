package com.github.agabrys.k8s.learn.k8scontroller;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.extended.controller.builder.ControllerBuilder;
import io.kubernetes.client.extended.controller.reconciler.Reconciler;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.extended.controller.reconciler.Result;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

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

  private static class DeploymentWithLabelReconciler implements Reconciler {

    private final Lister<V1Deployment> lister;
    private final LabelChecker labelChecker;
    private final CounterService counterService;

    public DeploymentWithLabelReconciler(
      SharedIndexInformer<V1Deployment> indexInformer, LabelChecker labelChecker, CounterService counterService
    ) {
      lister = new Lister<>(indexInformer.getIndexer());
      this.labelChecker = labelChecker;
      this.counterService = counterService;
    }

    @Override
    public Result reconcile(Request request) {
      var deployment = lister.namespace(request.getNamespace()).get(request.getName());
      if (labelChecker.hasLabel(deployment)) {
        counterService.increment();
      } else {
        counterService.decrement();
      }
      return new Result(false);
    }
  }

  private static class LabelChecker {

    private final String labelName;
    private final String labelValue;

    public LabelChecker(String labelName, String labelValue) {
      this.labelName = labelName;
      this.labelValue = labelValue;
    }

    public boolean hasLabel(KubernetesObject object) {
      return labelValue.equals(readLabels(object).get(labelName));
    }

    private static Map<String, String> readLabels(KubernetesObject object) {
      if (object == null) {
        return Collections.emptyMap();
      }
      var labels = object.getMetadata().getLabels();
      return labels != null ? labels : Collections.emptyMap();
    }
  }
}

package com.github.agabrys.k8s.learn.k8scontroller;

import io.kubernetes.client.extended.controller.reconciler.Reconciler;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.extended.controller.reconciler.Result;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.models.V1Deployment;

public class DeploymentWithLabelReconciler implements Reconciler {

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

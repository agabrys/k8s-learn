package com.github.agabrys.k8s.learn.k8scontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.github.agabrys.k8s.learn.k8scontroller.CounterService.CounterServiceException;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.cache.Indexer;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DeploymentWithLabelReconcilerTest {

  private static final String DEFAULT_NAME = "deployment";
  private static final String DEFAULT_NAMESPACE = "default";

  private DeploymentWithLabelReconciler reconciler;
  private Indexer<V1Deployment> indexer;
  private LabelChecker labelChecker;
  private CounterService counterService;

  @BeforeEach
  void setUp() {
    var indexInformer = mock(SharedIndexInformer.class);
    indexer = mock(Indexer.class);
    when(indexInformer.getIndexer()).thenReturn(indexer);
    labelChecker = mock(LabelChecker.class);
    counterService = mock(CounterService.class);
    reconciler = new DeploymentWithLabelReconciler(indexInformer, labelChecker, counterService);
  }

  @Test
  void verifyReconcileWhenLabelHasBeenRemoved() {
    var deployment = newDeploymentBuilder().build();
    addDeploymentToIndexer(deployment);
    when(labelChecker.hasLabel(deployment)).thenReturn(Boolean.FALSE);

    var result = reconciler.reconcile(newRequest());

    assertThat(result).isNotNull();
    assertThat(result.isRequeue()).isFalse();
    verify(counterService).decrement();
    verifyNoMoreInteractions(counterService);
  }

  @Test
  void verifyReconcileWhenDeploymentHasDifferentNamespace() {
    var deployment = newDeploymentBuilder().namespace("other").build();
    addDeploymentToIndexer(deployment);
    when(labelChecker.hasLabel(null)).thenReturn(Boolean.FALSE);

    var result = reconciler.reconcile(newRequest());

    assertThat(result).isNotNull();
    assertThat(result.isRequeue()).isFalse();
    verify(counterService).decrement();
    verifyNoMoreInteractions(counterService);
  }

  @Test
  void verifyReconcileWhenDeploymentHasDifferentName() {
    var deployment = newDeploymentBuilder().name("other").build();
    addDeploymentToIndexer(deployment);
    when(labelChecker.hasLabel(null)).thenReturn(Boolean.FALSE);

    var result = reconciler.reconcile(newRequest());

    assertThat(result).isNotNull();
    assertThat(result.isRequeue()).isFalse();
    verify(counterService).decrement();
    verifyNoMoreInteractions(counterService);
  }

  @Test
  void verifyReconcileWhenLabelHasBeenAdded() {
    var deployment = newDeploymentBuilder().build();
    addDeploymentToIndexer(deployment);
    when(labelChecker.hasLabel(deployment)).thenReturn(Boolean.TRUE);

    var result = reconciler.reconcile(newRequest());

    assertThat(result).isNotNull();
    assertThat(result.isRequeue()).isFalse();
    verify(counterService).increment();
    verifyNoMoreInteractions(counterService);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void verifyReconcileWhenCounterServiceThrowsException(boolean hasLabel) {
    when(labelChecker.hasLabel(null)).thenReturn(hasLabel);
    doThrow(CounterServiceException.class).when(counterService).increment();
    doThrow(CounterServiceException.class).when(counterService).decrement();

    assertThrows(CounterServiceException.class, () -> reconciler.reconcile(newRequest()));
  }

  private void addDeploymentToIndexer(V1Deployment deployment) {
    var metadata = deployment.getMetadata();
    var key = metadata.getNamespace() + '/' + metadata.getName();
    when(indexer.getByKey(anyString())).thenAnswer(invocation -> {
      var wantedKey = invocation.getArgument(0, String.class);
      return key.equals(wantedKey) ? deployment : null;
    });
  }

  private static DeploymentBuilder newDeploymentBuilder() {
    return new DeploymentBuilder();
  }

  private static Request newRequest() {
    return new Request(DEFAULT_NAMESPACE, DEFAULT_NAME);
  }

  private static class DeploymentBuilder {

    private String name;
    private String namespace;

    DeploymentBuilder name(String name) {
      this.name = name;
      return this;
    }

    DeploymentBuilder namespace(String namespace) {
      this.namespace = namespace;
      return this;
    }

    V1Deployment build() {
      var metadata = mock(V1ObjectMeta.class);
      when(metadata.getName()).thenReturn(name != null ? name : DEFAULT_NAME);
      when(metadata.getNamespace()).thenReturn(namespace != null ? namespace : DEFAULT_NAMESPACE);

      var deployment = mock(V1Deployment.class);
      when(deployment.getMetadata()).thenReturn(metadata);
      return deployment;
    }
  }
}

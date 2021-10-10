package com.github.agabrys.k8s.learn.k8scontroller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LabelCheckerTest {

  private static final String LABEL_NAME = "name";
  private static final String LABEL_VALUE = "value";

  private LabelChecker checker;

  @BeforeEach
  void setUp() {
    checker = new LabelChecker(LABEL_NAME, LABEL_VALUE);
  }

  @Test
  void verifyHasLabelWhenObjectIsNull() {
    assertThat(checker.hasLabel(null)).isFalse();
  }

  @Test
  void verifyHasLabelWhenObjectHasNoLabels() {
    var object = mock(KubernetesObject.class);
    var metadata = mock(V1ObjectMeta.class);
    when(object.getMetadata()).thenReturn(metadata);

    var result = checker.hasLabel(object);

    assertThat(result).isFalse();
  }

  @Test
  void verifyHasLabelWhenObjectDoesNotHaveRequiredLabel() {
    var object = mock(KubernetesObject.class);
    var metadata = mock(V1ObjectMeta.class);
    when(object.getMetadata()).thenReturn(metadata);
    when(metadata.getLabels()).thenReturn(Map.of(
      "foo", "bar",
      LABEL_NAME, "different",
      "different", LABEL_VALUE)
    );

    var result = checker.hasLabel(object);

    assertThat(result).isFalse();
  }

  @Test
  void verifyHasLabelWhenObjectHasRequiredLabel() {
    var object = mock(KubernetesObject.class);
    var metadata = mock(V1ObjectMeta.class);
    when(object.getMetadata()).thenReturn(metadata);
    when(metadata.getLabels()).thenReturn(Map.of(LABEL_NAME, LABEL_VALUE));

    var result = checker.hasLabel(object);

    assertThat(result).isTrue();
  }
}

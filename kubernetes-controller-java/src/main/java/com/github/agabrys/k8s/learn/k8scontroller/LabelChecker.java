package com.github.agabrys.k8s.learn.k8scontroller;

import io.kubernetes.client.common.KubernetesObject;
import java.util.Collections;
import java.util.Map;

public class LabelChecker {

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

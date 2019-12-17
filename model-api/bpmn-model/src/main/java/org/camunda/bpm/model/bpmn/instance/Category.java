package org.camunda.bpm.model.bpmn.instance;
import java.util.Collection;

public interface Category extends RootElement {

  String getName();

  void setName(String name);

  Collection<CategoryValue> getCategoryValues();
}
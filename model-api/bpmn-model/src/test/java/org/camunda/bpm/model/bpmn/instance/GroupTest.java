package org.camunda.bpm.model.bpmn.instance;

import java.util.Arrays;
import java.util.Collection;

public class GroupTest extends BpmnModelElementInstanceTest {

  @Override
  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(Artifact.class, false);
  }

  @Override
  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return null;
  }

  @Override
  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(new AttributeAssumption("categoryValueRef", false, false));
  }
}
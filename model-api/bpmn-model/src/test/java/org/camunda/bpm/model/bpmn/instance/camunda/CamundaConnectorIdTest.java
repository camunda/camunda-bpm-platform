package org.camunda.bpm.model.bpmn.instance.camunda;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

import java.util.Collection;

import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstanceTest;

public class CamundaConnectorIdTest extends BpmnModelElementInstanceTest {

  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(CAMUNDA_NS, false);
  }

  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return null;
  }

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return null;
  }

}

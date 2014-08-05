package org.camunda.bpm.model.bpmn.instance.camunda;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

import java.util.Arrays;
import java.util.Collection;

import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstanceTest;

public class CamundaConnectorTest extends BpmnModelElementInstanceTest {

  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(CAMUNDA_NS, false);
  }

  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return Arrays.asList(
      new ChildElementAssumption(CAMUNDA_NS, CamundaConnectorId.class, 1, 1),
      new ChildElementAssumption(CAMUNDA_NS, CamundaInputOutput.class, 0, 1)
    );
  }

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return null;
  }

}

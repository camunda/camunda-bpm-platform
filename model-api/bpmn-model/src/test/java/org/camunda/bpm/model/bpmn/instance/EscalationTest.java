package org.camunda.bpm.model.bpmn.instance;

import java.util.Arrays;
import java.util.Collection;

public class EscalationTest extends BpmnModelElementInstanceTest {

  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(RootElement.class, false);
  }

  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return null;
  }

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return Arrays.asList(
      new AttributeAssumption("name"),
      new AttributeAssumption("escalationCode"),
      new AttributeAssumption("structureRef")
    );
  }

}

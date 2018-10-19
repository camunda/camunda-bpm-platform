package org.camunda.bpm.model.bpmn.instance.camunda;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.camunda.bpm.model.bpmn.instance.BpmnModelElementInstanceTest;
import org.junit.Ignore;
import org.junit.Test;

public class CamundaListTest extends BpmnModelElementInstanceTest {

  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(CAMUNDA_NS, false);
  }

  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return null;
  }

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return null;
  }

  @Ignore("Test ignored. CAM-9441: Bug fix needed")
  @Test
  public void testListValueChildAssignment() {
    try {
      CamundaList listElement = modelInstance.newInstance(CamundaList.class);

      CamundaValue valueElement = modelInstance.newInstance(CamundaValue.class);
      valueElement.setTextContent("test");

      listElement.addChildElement(valueElement);

    } catch (Exception e) {
      fail("CamundaValue should be accepted as a child element of CamundaList. Error: " + e.getMessage());
    }
  }
}

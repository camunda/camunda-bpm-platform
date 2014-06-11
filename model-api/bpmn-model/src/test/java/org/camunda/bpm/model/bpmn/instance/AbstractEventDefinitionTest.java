package org.camunda.bpm.model.bpmn.instance;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.Query;
import org.camunda.bpm.model.bpmn.impl.QueryImpl;
import org.camunda.bpm.model.xml.impl.util.ReflectUtil;
import org.junit.Before;

import java.io.InputStream;
import java.util.Collection;

public abstract class AbstractEventDefinitionTest extends BpmnModelElementInstanceTest {

  protected Query<EventDefinition> eventDefinitionQuery;

  public TypeAssumption getTypeAssumption() {
    return new TypeAssumption(EventDefinition.class, false);
  }

  public Collection<ChildElementAssumption> getChildElementAssumptions() {
    return null;
  }

  public Collection<AttributeAssumption> getAttributesAssumptions() {
    return null;
  }

  @Before
  public void getEvent() {
    InputStream inputStream = ReflectUtil.getResourceAsStream("org/camunda/bpm/model/bpmn/EventDefinitionsTest.xml");
    IntermediateThrowEvent event = Bpmn.readModelFromStream(inputStream).getModelElementById("event");
    eventDefinitionQuery = new QueryImpl<EventDefinition>(event.getEventDefinitions());
  }

}

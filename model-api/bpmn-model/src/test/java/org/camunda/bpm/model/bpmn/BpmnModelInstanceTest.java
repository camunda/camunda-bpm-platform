package org.camunda.bpm.model.bpmn;

import org.camunda.bpm.model.bpmn.instance.Definitions;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Filip Hrisafov
 */
public class BpmnModelInstanceTest {

  @Test
  public void testClone() throws Exception {

    BpmnModelInstance modelInstance = Bpmn.createEmptyModel();

    Definitions definitions = modelInstance.newInstance(Definitions.class);
    definitions.setId("TestId");
    modelInstance.setDefinitions(definitions);

    BpmnModelInstance cloneInstance = modelInstance.clone();
    cloneInstance.getDefinitions().setId("TestId2");

    assertThat(modelInstance.getDefinitions().getId(), is(equalTo("TestId")));
    assertThat(cloneInstance.getDefinitions().getId(), is(equalTo("TestId2")));
  }

}

package org.camunda.bpm.model.dmn;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.model.dmn.instance.Definitions;
import org.junit.Test;

/**
 * @author Filip Hrisafov
 */
public class DmnModelInstanceTest {

  @Test
  public void testClone() throws Exception {

    DmnModelInstance modelInstance = Dmn.createEmptyModel();

    Definitions definitions = modelInstance.newInstance(Definitions.class);
    definitions.setId("TestId");
    modelInstance.setDefinitions(definitions);

    DmnModelInstance cloneInstance = modelInstance.clone();
    cloneInstance.getDefinitions().setId("TestId2");

    assertThat(modelInstance.getDefinitions().getId(), is(equalTo("TestId")));
    assertThat(cloneInstance.getDefinitions().getId(), is(equalTo("TestId2")));
  }

}

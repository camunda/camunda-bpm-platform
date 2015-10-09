package org.camunda.bpm.model.cmmn;

import org.camunda.bpm.model.cmmn.instance.Definitions;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Filip Hrisafov
 */
public class CmmnModelInstanceTest {

  @Test
  public void testClone() throws Exception {

    CmmnModelInstance modelInstance = Cmmn.createEmptyModel();

    Definitions definitions = modelInstance.newInstance(Definitions.class);
    definitions.setId("TestId");
    modelInstance.setDefinitions(definitions);

    CmmnModelInstance cloneInstance = modelInstance.clone();
    cloneInstance.getDefinitions().setId("TestId2");

    assertThat(modelInstance.getDefinitions().getId(), is(equalTo("TestId")));
    assertThat(cloneInstance.getDefinitions().getId(), is(equalTo("TestId2")));
  }

}

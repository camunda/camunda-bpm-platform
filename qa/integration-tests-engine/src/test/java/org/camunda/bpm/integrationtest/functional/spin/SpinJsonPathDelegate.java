package org.camunda.bpm.integrationtest.functional.spin;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.json.SpinJsonNode;

import static org.camunda.spin.Spin.JSON;

/**
 * Created by hawky4s on 04.05.15.
 */
public class SpinJsonPathDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String  json = "{\"child\": [{\"id\": 1,\"name\": \"Lucy\",\"sex\": \"female\"},{\"id\": 2,\"name\": \"Tracy\",\"sex\": \"female\"}],\"number\": 1,\"boolean\": true}";
    SpinJsonNode spinJsonNode = JSON(json).jsonPath("$.child[0]").element();

    if (spinJsonNode == null) {
      throw new RuntimeException("Unable to evalute jsonpath");
    }
  }

}

package org.camunda.bpm.pa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class ChangeVariablesService implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    Date now = new Date();

    List<String> serializable = new ArrayList<String>();
    serializable.add("four");
    serializable.add("five");
    serializable.add("six");

    byte[] bytes = "anotherBytes".getBytes();

    Map<String, Object> variables = new HashMap<String, Object>();

    variables.put("shortVar", (short) 456);
    variables.put("longVar", 999999L);
    variables.put("integerVar", 56789);

    variables.put("floatVar", 99.99);
    variables.put("doubleVar", 1200.4005);

    variables.put("trueBooleanVar", false);
    variables.put("falseBooleanVar", true);

    variables.put("stringVar", "sprite");

    variables.put("dateVar", now);


    variables.put("serializableVar", serializable);

    variables.put("bytesVar", bytes);
    variables.put("value1", "abc");

    int random = (int)(Math.random() * 100);
    variables.put("random", random);

    execution.setVariablesLocal(variables);
  }

}

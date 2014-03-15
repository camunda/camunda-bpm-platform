package org.camunda.bpm.pa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class AnotherChangeVariablesService implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    Date now = new Date();

    List<String> serializable = new ArrayList<String>();
    serializable.add("seven");
    serializable.add("eight");
    serializable.add("nine");

    byte[] bytes = "someAnotherBytes".getBytes();

    Map<String, Object> variables = new HashMap<String, Object>();

    variables.put("shortVar", (short) 789);
    variables.put("longVar", 555555L);
    variables.put("integerVar", 963852);

    variables.put("floatVar", 55.55);
    variables.put("doubleVar", 6123.2025);

    variables.put("trueBooleanVar", true);
    variables.put("falseBooleanVar", false);

    variables.put("stringVar", "fanta");

    variables.put("dateVar", now);

    variables.put("serializableVar", serializable);

    variables.put("bytesVar", bytes);
    variables.put("value1", "blub");

    int random = (int)(Math.random() * 100);
    variables.put("random", random);

    execution.setVariablesLocal(variables);
  }

}

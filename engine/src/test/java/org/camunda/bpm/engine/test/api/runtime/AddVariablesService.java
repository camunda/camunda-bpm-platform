package org.camunda.bpm.engine.test.api.runtime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class AddVariablesService implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    Date now = new Date();
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");
    
    // Start process instance with different types of variables
    Map<String, Object> variables = new HashMap<String, Object>();
    
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("longVar", 928374L);
       
    variables.put("stringVar", "coca-cola");
    variables.put("dateVar", now);
    variables.put("nullVar", null);
    variables.put("serializableVar", serializable);
    
    execution.setVariablesLocal(variables);

  }

}

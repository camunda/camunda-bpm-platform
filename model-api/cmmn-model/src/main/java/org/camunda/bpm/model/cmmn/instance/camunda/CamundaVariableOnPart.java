package org.camunda.bpm.model.cmmn.instance.camunda;

import org.camunda.bpm.model.cmmn.VariableTransition;
import org.camunda.bpm.model.cmmn.instance.CmmnModelElementInstance;
/**
 * 
 * @author Deivarayan Azhagappan
 *
 */
public interface CamundaVariableOnPart extends CmmnModelElementInstance {

  String getVariableName();

  void setVariableName(String variableName);

  VariableTransition getVariableEvent();

  void setVariableEvent(VariableTransition standardEvent);
}

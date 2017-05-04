package org.camunda.bpm.engine.impl;

import java.util.List;

import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;

/**
 * 
 * @author Anna Pazola
 *
 */
public class RestartProcessInstancesBatchConfiguration extends BatchConfiguration {

  protected List<AbstractProcessInstanceModificationCommand> instructions;
  protected String processDefinitionId;

  public RestartProcessInstancesBatchConfiguration(List<String> processInstanceIds, List<AbstractProcessInstanceModificationCommand> instructions, String processDefinitionId) {
    super(processInstanceIds);
    this.instructions = instructions;
    this.processDefinitionId = processDefinitionId;
  }

  public List<AbstractProcessInstanceModificationCommand> getInstructions() {
    return instructions;
  }

  public void setInstructions(List<AbstractProcessInstanceModificationCommand> instructions) {
    this.instructions = instructions;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }
}

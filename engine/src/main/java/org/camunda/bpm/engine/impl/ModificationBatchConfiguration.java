package org.camunda.bpm.engine.impl;

import java.util.List;

import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;

public class ModificationBatchConfiguration extends BatchConfiguration {

  protected List<AbstractProcessInstanceModificationCommand> instructions;
  protected boolean skipCustomListeners;
  protected boolean skipIoMappings;
  protected String processDefinitionId;

  public ModificationBatchConfiguration(List<String> ids, String processDefinitionId, List<AbstractProcessInstanceModificationCommand> instructions,
      boolean skipCustomListeners, boolean skipIoMappings) {
    super(ids);
    this.instructions = instructions;
    this.processDefinitionId = processDefinitionId;
    this.skipCustomListeners = skipCustomListeners;
    this.skipIoMappings = skipIoMappings;
  }

  public ModificationBatchConfiguration(List<String> ids, String processDefinitionId) {
    this(ids, processDefinitionId,  null, false, false);
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

  public boolean isSkipCustomListeners() {
    return skipCustomListeners;
  }

  public void setSkipCustomListeners(boolean skipCustomListeners) {
    this.skipCustomListeners = skipCustomListeners;
  }

  public boolean isSkipIoMappings() {
    return skipIoMappings;
  }

  public void setSkipIoMappings(boolean skipIoMappings) {
    this.skipIoMappings = skipIoMappings;
  }

}

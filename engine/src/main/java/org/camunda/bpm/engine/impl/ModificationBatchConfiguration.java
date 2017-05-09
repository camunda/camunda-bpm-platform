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

  public List<AbstractProcessInstanceModificationCommand> getInstructions() {
    return instructions;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public boolean isSkipCustomListeners() {
    return skipCustomListeners;
  }

  public boolean isSkipIoMappings() {
    return skipIoMappings;
  }

}

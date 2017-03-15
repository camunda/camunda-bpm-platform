package org.camunda.bpm.engine.impl;

import java.util.List;

import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;

public class ModificationBatchConfiguration extends BatchConfiguration {

  protected List<AbstractProcessInstanceModificationCommand> instructions;
  protected boolean skipCustomListeners;
  protected boolean skipIoMappings;

  public ModificationBatchConfiguration(List<String> ids, List<AbstractProcessInstanceModificationCommand> instructions,
      boolean skipCustomListeners, boolean skipIoMappings) {
    super(ids);
    this.instructions = instructions;
    this.skipCustomListeners = skipCustomListeners;
    this.skipIoMappings = skipIoMappings;
  }

  public ModificationBatchConfiguration(List<String> ids) {
    this(ids, null, false, false);
  }

  public List<AbstractProcessInstanceModificationCommand> getInstructions() {
    return instructions;
  }

  public void setInstructions(List<AbstractProcessInstanceModificationCommand> instructions) {
    this.instructions = instructions;
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

package org.activiti.engine.impl.persistence.entity;

import org.activiti.engine.impl.db.ListQueryParameterObject;

public class StatisticsSqlParameters extends ListQueryParameterObject {

  private String processDefinitionId;

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }
}

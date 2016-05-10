package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;


public class GetTableNameCmd implements Command<String>, Serializable {

  private static final long serialVersionUID = 1L;

  private Class<?> entityClass;

  public GetTableNameCmd(Class< ? > entityClass) {
    this.entityClass = entityClass;
  }

  public String execute(CommandContext commandContext) {
    ensureNotNull("entityClass", entityClass);

    commandContext.getAuthorizationManager().checkCamundaAdmin();

    return commandContext
      .getTableDataManager()
      .getTableName(entityClass, true);
  }

}

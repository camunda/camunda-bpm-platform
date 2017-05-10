package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;

import java.io.Serializable;
import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

public class DeleteHistoricCaseInstancesBulkCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  protected final List<String> caseInstanceIds;

  public DeleteHistoricCaseInstancesBulkCmd(List<String> caseInstanceIds) {
    this.caseInstanceIds = caseInstanceIds;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    ensureNotEmpty(BadUserRequestException.class, "caseInstanceIds", caseInstanceIds);

    commandContext.getHistoricCaseInstanceManager().deleteHistoricCaseInstancesByIds(caseInstanceIds);

    return null;
  }

}
package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureEquals;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.HistoricCaseInstanceQueryImpl;
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

    // Check if case instances are all closed
    commandContext.runWithoutAuthorization(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        ensureEquals(BadUserRequestException.class, "ClosedCaseInstanceIds",
            new HistoricCaseInstanceQueryImpl().closed().caseInstanceIds(new HashSet<String>(caseInstanceIds)).count(), caseInstanceIds.size());
        return null;
      }
    });

    commandContext.getHistoricCaseInstanceManager().deleteHistoricCaseInstancesByIds(caseInstanceIds);

    return null;
  }

}
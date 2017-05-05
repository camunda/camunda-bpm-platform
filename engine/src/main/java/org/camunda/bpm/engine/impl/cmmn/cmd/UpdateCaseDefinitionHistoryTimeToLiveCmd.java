package org.camunda.bpm.engine.impl.cmmn.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureGreaterThanOrEqual;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

public class UpdateCaseDefinitionHistoryTimeToLiveCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String caseDefinitionId;
  protected Integer historyTimeToLive;

  public UpdateCaseDefinitionHistoryTimeToLiveCmd(String caseDefinitionId, Integer historyTimeToLive) {
    this.caseDefinitionId = caseDefinitionId;
    this.historyTimeToLive = historyTimeToLive;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    ensureNotNull(BadUserRequestException.class, "caseDefinitionId", caseDefinitionId);

    if (historyTimeToLive != null) {
      ensureGreaterThanOrEqual(BadUserRequestException.class, "", "historyTimeToLive", historyTimeToLive, 0);
    }

    CaseDefinitionEntity caseDefinitionEntity = commandContext.getCaseDefinitionManager().findLatestDefinitionById(caseDefinitionId);

    for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkUpdateCaseDefinition(caseDefinitionEntity);
    }

    caseDefinitionEntity.setHistoryTimeToLive(historyTimeToLive);

    return null;
  }
}

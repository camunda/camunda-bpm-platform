package org.camunda.bpm.engine.impl.cmd;

import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

public class SetExternalTasksRetriesCmd extends AbstractSetExternalTaskRetriesCmd<Void> {

  public SetExternalTasksRetriesCmd(List<String> externalTaskIds, int retries) {
    super(externalTaskIds, null, retries);
  }

  public SetExternalTasksRetriesCmd(List<String> externalTaskIds, ExternalTaskQuery externalTaskQuery, ProcessInstanceQuery processInstanceQuery, HistoricProcessInstanceQuery historicProcessInstanceQuery, int retries) {
    super(externalTaskIds, externalTaskQuery, processInstanceQuery, historicProcessInstanceQuery, retries);
  }

  public SetExternalTasksRetriesCmd(ProcessInstanceQuery processInstanceQuery, int retries) {
    super(processInstanceQuery, retries);
  }

  public SetExternalTasksRetriesCmd(HistoricProcessInstanceQuery historicProcessInstanceQuery, int retries) {
    super(historicProcessInstanceQuery, retries);
  }

  @Override
  public Void execute(CommandContext commandContext) {
    List<String> collectedIds = collectExternalTaskIds();
    EnsureUtil.ensureNotEmpty(BadUserRequestException.class, "externalTaskIds", collectedIds);

    writeUserOperationLog(commandContext,
        retries,
        collectedIds.size(),
        false);

    for (String externalTaskId : collectedIds) {
      new SetExternalTaskRetriesCmd(externalTaskId, retries, false).execute(commandContext);
    }

    return null;
  }
}

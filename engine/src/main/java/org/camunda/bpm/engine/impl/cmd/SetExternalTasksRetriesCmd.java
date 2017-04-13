package org.camunda.bpm.engine.impl.cmd;

import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

public class SetExternalTasksRetriesCmd extends AbstractSetExternalTaskRetriesCmd<Void> {

  protected final int retries;

  public SetExternalTasksRetriesCmd(List<String> externalTaskIds, ExternalTaskQuery externalTaskQuery, int retries) {
    super(externalTaskIds, externalTaskQuery);
    this.retries = retries;
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

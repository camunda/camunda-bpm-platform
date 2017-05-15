package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotContainsNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.ExternalTaskQueryImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

public abstract class AbstractSetExternalTaskRetriesCmd<T> implements Command<T> {

  protected List<String> externalTaskIds;
  protected ExternalTaskQuery externalTaskQuery;
  protected int retries;

  public AbstractSetExternalTaskRetriesCmd(List<String> externalTaskIds, ExternalTaskQuery externalTaskQuery, int retries) {
    this.externalTaskIds = externalTaskIds;
    this.externalTaskQuery = externalTaskQuery;
    this.retries = retries;
  }

  protected List<String> collectExternalTaskIds() {

    Set<String> collectedIds = new HashSet<String>();

    if (externalTaskIds != null) {
      ensureNotContainsNull(BadUserRequestException.class, "External task id cannot be null", "externalTaskIds", externalTaskIds);
      collectedIds.addAll(externalTaskIds);
    }

    if (externalTaskQuery != null) {
      List<String> ids = Context.getCommandContext().runWithoutAuthorization(new Callable<List<String>>(){

        @Override
        public List<String> call() throws Exception {
          return ((ExternalTaskQueryImpl) externalTaskQuery).listIds();
        }
      });

      collectedIds.addAll(ids);
    }
    return new ArrayList<String>(collectedIds);
  }

  protected void writeUserOperationLog(CommandContext commandContext, int retries, int numInstances, boolean async) {

    List<PropertyChange> propertyChanges = new ArrayList<PropertyChange>();
    propertyChanges.add(new PropertyChange("nrOfInstances", null, numInstances));
    propertyChanges.add(new PropertyChange("async", null, async));
    propertyChanges.add(new PropertyChange("retries", null, retries));

    commandContext.getOperationLogManager().logProcessInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_SET_EXTERNAL_TASK_RETRIES, null, null, null,
        propertyChanges);
  }
}

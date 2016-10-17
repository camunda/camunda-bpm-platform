package org.camunda.bpm.engine.impl;

import org.camunda.bpm.engine.impl.cmd.AbstractSetJobsRetriesBatchCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Askar Akhmerov
 */
public class SetJobsRetriesByProcessBatchCmd extends AbstractSetJobsRetriesBatchCmd {
  protected final List<String> processInstanceIds;
  protected final ProcessInstanceQuery query;

  public SetJobsRetriesByProcessBatchCmd(List<String> processInstanceIds, ProcessInstanceQuery query, int retries) {
    this.processInstanceIds = processInstanceIds;
    this.query = query;
    this.retries = retries;
  }

  protected List<String> collectJobIds(CommandContext commandContext) {
    List<String> collectedJobIds = new ArrayList<String>();

    if (this.processInstanceIds != null) {
      for (String process : this.processInstanceIds) {
        for (Job job : commandContext.getJobManager().findJobsByExecutionId(process)) {
          collectedJobIds.add(job.getId());
        }
      }
    }

    if (query != null) {
      for (ProcessInstance process : query.list()) {
        for (Job job : commandContext.getJobManager().findJobsByExecutionId(process.getId())) {
          collectedJobIds.add(job.getId());
        }
      }
    }

    return collectedJobIds;
  }
}

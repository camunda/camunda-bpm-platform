package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;

/**
 * @author Saeid Mirzaei
 * @author Joram Barrez
 */

public class DeleteJobCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String jobId;

  public DeleteJobCmd(String jobId) {
    this.jobId = jobId;
  }

  public Object execute(CommandContext commandContext) {
    ensureNotNull("jobId", jobId);

    JobEntity job = commandContext.getJobManager().findJobById(jobId);
    ensureNotNull("No job found with id '" + jobId + "'", "job", job);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkUpdateJob(job);
    }
    // We need to check if the job was locked, ie acquired by the job acquisition thread
    // This happens if the the job was already acquired, but not yet executed.
    // In that case, we can't allow to delete the job.
    if (job.getLockOwner() != null || job.getLockExpirationTime() != null) {
      throw new ProcessEngineException("Cannot delete job when the job is being executed. Try again later.");
    }

    job.delete();
    return null;
  }

}

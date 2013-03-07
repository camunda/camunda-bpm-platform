package org.camunda.bpm.engine.impl.jobexecutor.tobemerged.impl.acquisition;

import java.util.List;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.RejectedJobsHandler;
import org.camunda.bpm.engine.impl.jobexecutor.tobemerged.JobExecutorService;



/**
 * <p>Delegates to a {@link JobAcquisition}. This delegate JobExecutor is passed to
 * process engine.</p>
 * 
 * <p>The actual {@link JobAcquisition} instance is not cached here since it might
 * be removed while the process engine is still up. If we cache it here this
 * might lead to memory leaks.</p>
 * 
 * @author Daniel Meyer
 */
public class PlatformJobExecutorDelegate extends JobExecutor {

  protected final JobExecutorService platformJobExecutor;
  protected final String acquisitionName;
  
  public PlatformJobExecutorDelegate(JobExecutorService platformJobExecutor, String acquisitionName) {
    this.platformJobExecutor = platformJobExecutor;
    this.acquisitionName = acquisitionName;
  }
  
  protected JobExecutor getDelegate() {
    return platformJobExecutor.getJobAcquisitionByName(acquisitionName);
  }
 
  public void start() {
    getDelegate().start(); // TODO: prohibit?
  }

  public void shutdown() {
    getDelegate().shutdown();  // TODO: prohibit?
  }

  public void jobWasAdded() {
    getDelegate().jobWasAdded();
  }

  public CommandExecutor getCommandExecutor() {
    return getDelegate().getCommandExecutor();
  }

  public int getWaitTimeInMillis() {
    return getDelegate().getWaitTimeInMillis();
  }

  public void setWaitTimeInMillis(int waitTimeInMillis) {
    getDelegate().setWaitTimeInMillis(waitTimeInMillis);
  }

  public int getLockTimeInMillis() {
    return getDelegate().getLockTimeInMillis();
  }

  public void setLockTimeInMillis(int lockTimeInMillis) {
    getDelegate().setLockTimeInMillis(lockTimeInMillis);
  }

  public String getLockOwner() {
    return getDelegate().getLockOwner();
  }

  public boolean equals(Object obj) {
    return getDelegate().equals(obj);
  }

  public void setLockOwner(String lockOwner) {
    getDelegate().setLockOwner(lockOwner);
  }

  public boolean isAutoActivate() {
    return getDelegate().isAutoActivate();
  }

  public void setCommandExecutor(CommandExecutor commandExecutor) {
    getDelegate().setCommandExecutor(commandExecutor);
  }

  public void setAutoActivate(boolean isAutoActivate) {
    getDelegate().setAutoActivate(isAutoActivate);
  }

  public int getMaxJobsPerAcquisition() {
    return getDelegate().getMaxJobsPerAcquisition();
  }

  public void setMaxJobsPerAcquisition(int maxJobsPerAcquisition) {
    getDelegate().setMaxJobsPerAcquisition(maxJobsPerAcquisition);
  }

  public String getName() {
    return getDelegate().getName();
  }

  public Command<AcquiredJobs> getAcquireJobsCmd() {
    return getDelegate().getAcquireJobsCmd();
  }

  public void setAcquireJobsCmd(Command<AcquiredJobs> acquireJobsCmd) {
    getDelegate().setAcquireJobsCmd(acquireJobsCmd);
  }

  public boolean isActive() {
    return getDelegate().isActive();
  }

  public RejectedJobsHandler getRejectedJobsHandler() {
    return getDelegate().getRejectedJobsHandler();
  }

  public void setRejectedJobsHandler(RejectedJobsHandler rejectedJobsHandler) {
    getDelegate().setRejectedJobsHandler(rejectedJobsHandler);
  }

  @Override
  protected void startExecutingJobs() {
    // TODO Auto-generated method stub    
  }

  @Override
  protected void stopExecutingJobs() {
    // TODO Auto-generated method stub    
  }

  @Override
  protected void executeJobs(List<String> jobIds) {
    // TODO Auto-generated method stub    
  }
  
}

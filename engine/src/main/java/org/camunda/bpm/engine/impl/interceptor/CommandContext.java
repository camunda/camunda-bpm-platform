/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.interceptor;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskAlreadyClaimedException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.TransactionContext;
import org.camunda.bpm.engine.impl.cfg.TransactionContextFactory;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionManager;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionManager;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseSentryPartManager;
import org.camunda.bpm.engine.impl.cmmn.operation.CmmnAtomicOperation;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.ProcessApplicationContextUtil;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSession;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionManager;
import org.camunda.bpm.engine.impl.history.event.HistoricDecisionInstanceManager;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider;
import org.camunda.bpm.engine.impl.identity.WritableIdentityProvider;
import org.camunda.bpm.engine.impl.jobexecutor.FailedJobCommandFactory;
import org.camunda.bpm.engine.impl.persistence.entity.AttachmentManager;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayManager;
import org.camunda.bpm.engine.impl.persistence.entity.CommentManager;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentManager;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionManager;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskManager;
import org.camunda.bpm.engine.impl.persistence.entity.FilterManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricActivityInstanceManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricCaseActivityInstanceManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricCaseInstanceManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceManager;
import org.camunda.bpm.engine.impl.persistence.entity.ReportManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricStatisticsManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceManager;
import org.camunda.bpm.engine.impl.persistence.entity.IdentityInfoManager;
import org.camunda.bpm.engine.impl.persistence.entity.IdentityLinkManager;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentManager;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.MeterLogManager;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyManager;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceManager;
import org.camunda.bpm.engine.impl.persistence.entity.StatisticsManager;
import org.camunda.bpm.engine.impl.persistence.entity.TableDataManager;
import org.camunda.bpm.engine.impl.persistence.entity.TaskManager;
import org.camunda.bpm.engine.impl.persistence.entity.UserOperationLogManager;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceManager;
import org.camunda.bpm.engine.impl.pvm.runtime.AtomicOperation;

/**
 * @author Tom Baeyens
 * @author Agim Emruli
 * @author Daniel Meyer
 */
public class CommandContext {

  private final static ContextLogger LOG = ProcessEngineLogger.CONTEXT_LOGGER;

  protected boolean authorizationCheckEnabled = true;
  protected boolean userOperationLogEnabled = true;

  protected TransactionContext transactionContext;
  protected Map<Class< ? >, SessionFactory> sessionFactories;
  protected Map<Class< ? >, Session> sessions = new HashMap<Class< ? >, Session>();
  protected List<Session> sessionList = new ArrayList<Session>();
  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected FailedJobCommandFactory failedJobCommandFactory;

  protected List<AtomicOperationInvocation> queuedInvocations = new ArrayList<AtomicOperationInvocation>();
  protected BpmnStackTrace bpmnStackTrace = new BpmnStackTrace();

  protected boolean isExecuting = false;

  protected List<CommandContextListener> commandContextListeners = new LinkedList<CommandContextListener>();

  public CommandContext(ProcessEngineConfigurationImpl processEngineConfiguration) {
    this(processEngineConfiguration, processEngineConfiguration.getTransactionContextFactory());
  }

  public CommandContext(ProcessEngineConfigurationImpl processEngineConfiguration, TransactionContextFactory transactionContextFactory) {
    this.processEngineConfiguration = processEngineConfiguration;
    this.failedJobCommandFactory = processEngineConfiguration.getFailedJobCommandFactory();
    sessionFactories = processEngineConfiguration.getSessionFactories();
    this.transactionContext = transactionContextFactory.openTransactionContext(this);
  }

  public void performOperation(AtomicOperation executionOperation, ExecutionEntity execution) {
    performOperation(executionOperation, execution, false);
  }

  public void performOperationAsync(AtomicOperation executionOperation, ExecutionEntity execution) {
    performOperation(executionOperation, execution, true);
  }

  public void performOperation(final AtomicOperation executionOperation, final ExecutionEntity execution, final boolean performAsync) {
    AtomicOperationInvocation invocation = new AtomicOperationInvocation(executionOperation, execution, performAsync);
    queuedInvocations.add(0, invocation);
    performNext();
  }

  protected void performNext() {
    AtomicOperationInvocation nextInvocation = queuedInvocations.get(0);

    if(nextInvocation.operation.isAsyncCapable() && isExecuting) {
      // will be picked up by while loop below
      return;
    }

    ProcessApplicationReference targetProcessApplication = getTargetProcessApplication(nextInvocation.execution);
    if(requiresContextSwitch(targetProcessApplication)) {

      Context.executeWithinProcessApplication(new Callable<Void>() {
        public Void call() throws Exception {
          performNext();
          return null;
        }

      }, targetProcessApplication);
    }
    else {
      if(!nextInvocation.operation.isAsyncCapable()) {
        // if operation is not async capable, perform right away.
        invokeNext();
      }
      else {
        try  {
          isExecuting = true;
          while (!queuedInvocations.isEmpty()) {
            // assumption: all operations are executed within the same process application...
            nextInvocation = queuedInvocations.get(0);
            invokeNext();
          }
        }
        finally {
          isExecuting = false;
        }
      }
    }
  }

  protected void invokeNext() {
    AtomicOperationInvocation invocation = queuedInvocations.remove(0);
    try {
      invocation.execute(bpmnStackTrace);
    }
    catch(RuntimeException e) {
      // log bpmn stacktrace
      bpmnStackTrace.printStackTrace(Context.getProcessEngineConfiguration().isBpmnStacktraceVerbose());
      // rethrow
      throw e;
    }
  }

  public void performOperation(final CmmnAtomicOperation executionOperation, final CaseExecutionEntity execution) {
    ProcessApplicationReference targetProcessApplication = getTargetProcessApplication(execution);

    if(requiresContextSwitch(targetProcessApplication)) {

      Context.executeWithinProcessApplication(new Callable<Void>() {
        public Void call() throws Exception {
          performOperation(executionOperation, execution);
          return null;
        }

      }, targetProcessApplication);

    } else {
      try {
        Context.setExecutionContext(execution);
        LOG.debugExecutingAtomicOperation(executionOperation, execution);

        executionOperation.execute(execution);
      } finally {
        Context.removeExecutionContext();
      }
    }
  }

  public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    return processEngineConfiguration;
  }

  protected ProcessApplicationReference getTargetProcessApplication(ExecutionEntity execution) {
    return ProcessApplicationContextUtil.getTargetProcessApplication(execution);
  }

  protected ProcessApplicationReference getTargetProcessApplication(CaseExecutionEntity execution) {
    return ProcessApplicationContextUtil.getTargetProcessApplication(execution);
  }

  protected boolean requiresContextSwitch(ProcessApplicationReference processApplicationReference) {
    return ProcessApplicationContextUtil.requiresContextSwitch(processApplicationReference);
  }

  public void close(CommandInvocationContext commandInvocationContext) {
    // the intention of this method is that all resources are closed properly,
    // even
    // if exceptions occur in close or flush methods of the sessions or the
    // transaction context.

    try {
      try {
        try {

          if (commandInvocationContext.getThrowable() == null) {
            fireCommandContextClose();
            flushSessions();
          }

        } catch (Throwable exception) {
          commandInvocationContext.trySetThrowable(exception);
        } finally {

          try {
            if (commandInvocationContext.getThrowable() == null) {
              transactionContext.commit();
            }
          } catch (Throwable exception) {
            commandInvocationContext.trySetThrowable(exception);
          }

          if (commandInvocationContext.getThrowable() != null) {
            // fire command failed (must not fail itself)
            fireCommandFailed(commandInvocationContext.getThrowable());

            Level loggingLevel = Level.SEVERE;
            if (shouldLogInfo(commandInvocationContext.getThrowable())) {
              LOG.infoException(commandInvocationContext.getThrowable());
            }
            else if (shouldLogFine(commandInvocationContext.getThrowable())) {
              LOG.debugException(commandInvocationContext.getThrowable());
            }
            else {
              LOG.errorException(commandInvocationContext.getThrowable());
            }
            transactionContext.rollback();
          }
        }
      } catch (Throwable exception) {
        commandInvocationContext.trySetThrowable(exception);
      } finally {
        closeSessions(commandInvocationContext);
      }
    } catch (Throwable exception) {
      commandInvocationContext.trySetThrowable(exception);
    }

    // rethrow the original exception if there was one
    commandInvocationContext.rethrow();
  }

  protected boolean shouldLogInfo(Throwable exception) {
    return exception instanceof TaskAlreadyClaimedException;
  }

  protected boolean shouldLogFine(Throwable exception) {
    return exception instanceof OptimisticLockingException || exception instanceof BadUserRequestException;
  }

  protected void fireCommandContextClose() {
    for (CommandContextListener listener : commandContextListeners) {
      listener.onCommandContextClose(this);
    }
  }

  protected void fireCommandFailed(Throwable t) {
    for (CommandContextListener listener : commandContextListeners) {
      try {
        listener.onCommandFailed(this, t);
      }
      catch(Throwable ex) {
        LOG.exceptionWhileInvokingOnCommandFailed(t);
      }
    }
  }

  protected void flushSessions() {
    for (int i = 0; i< sessionList.size(); i++) {
      sessionList.get(i).flush();
    }
  }

  protected void closeSessions(CommandInvocationContext commandInvocationContext) {
    for (Session session : sessionList) {
      try {
        session.close();
      } catch (Throwable exception) {
        commandInvocationContext.trySetThrowable(exception);
      }
    }
  }

  @SuppressWarnings({"unchecked"})
  public <T> T getSession(Class<T> sessionClass) {
    Session session = sessions.get(sessionClass);
    if (session == null) {
      SessionFactory sessionFactory = sessionFactories.get(sessionClass);
      ensureNotNull("no session factory configured for " + sessionClass.getName(), "sessionFactory", sessionFactory);
      session = sessionFactory.openSession();
      sessions.put(sessionClass, session);
      sessionList.add(0, session);
    }

    return (T) session;
  }

  public DbEntityManager getDbEntityManager() {
    return getSession(DbEntityManager.class);
  }

  public DbSqlSession getDbSqlSession() {
    return getSession(DbSqlSession.class);
  }

  public DeploymentManager getDeploymentManager() {
    return getSession(DeploymentManager.class);
  }

  public ResourceManager getResourceManager() {
    return getSession(ResourceManager.class);
  }

  public ByteArrayManager getByteArrayManager() {
    return getSession(ByteArrayManager.class);
  }

  public ProcessDefinitionManager getProcessDefinitionManager() {
    return getSession(ProcessDefinitionManager.class);
  }

  public ExecutionManager getExecutionManager() {
    return getSession(ExecutionManager.class);
  }

  public TaskManager getTaskManager() {
    return getSession(TaskManager.class);
  }

  public MeterLogManager getMeterLogManager() {
    return getSession(MeterLogManager.class);
  }

  public IdentityLinkManager getIdentityLinkManager() {
    return getSession(IdentityLinkManager.class);
  }

  public VariableInstanceManager getVariableInstanceManager() {
    return getSession(VariableInstanceManager.class);
  }

  public HistoricProcessInstanceManager getHistoricProcessInstanceManager() {
    return getSession(HistoricProcessInstanceManager.class);
  }

  public HistoricCaseInstanceManager getHistoricCaseInstanceManager() {
    return getSession(HistoricCaseInstanceManager.class);
  }

  public HistoricDetailManager getHistoricDetailManager() {
    return getSession(HistoricDetailManager.class);
  }

  public UserOperationLogManager getOperationLogManager() {
    return getSession(UserOperationLogManager.class);
  }

  public HistoricVariableInstanceManager getHistoricVariableInstanceManager() {
    return getSession(HistoricVariableInstanceManager.class);
  }

  public HistoricActivityInstanceManager getHistoricActivityInstanceManager() {
    return getSession(HistoricActivityInstanceManager.class);
  }

  public HistoricCaseActivityInstanceManager getHistoricCaseActivityInstanceManager() {
    return getSession(HistoricCaseActivityInstanceManager.class);
  }

  public HistoricTaskInstanceManager getHistoricTaskInstanceManager() {
    return getSession(HistoricTaskInstanceManager.class);
  }

  public HistoricIncidentManager getHistoricIncidentManager() {
    return getSession(HistoricIncidentManager.class);
  }

  public JobManager getJobManager() {
    return getSession(JobManager.class);
  }

  public JobDefinitionManager getJobDefinitionManager() {
    return getSession(JobDefinitionManager.class);
  }

  public IncidentManager getIncidentManager() {
    return getSession(IncidentManager.class);
  }

  public IdentityInfoManager getIdentityInfoManager() {
    return getSession(IdentityInfoManager.class);
  }

  public AttachmentManager getAttachmentManager() {
    return getSession(AttachmentManager.class);
  }

  public TableDataManager getTableDataManager() {
    return getSession(TableDataManager.class);
  }

  public CommentManager getCommentManager() {
    return getSession(CommentManager.class);
  }

  public EventSubscriptionManager getEventSubscriptionManager() {
    return getSession(EventSubscriptionManager.class);
  }

  public Map<Class< ? >, SessionFactory> getSessionFactories() {
    return sessionFactories;
  }

  public PropertyManager getPropertyManager() {
    return getSession(PropertyManager.class);
  }

  public StatisticsManager getStatisticsManager() {
    return getSession(StatisticsManager.class);
  }

  public HistoricStatisticsManager getHistoricStatisticsManager() {
    return getSession(HistoricStatisticsManager.class);
  }

  public HistoricJobLogManager getHistoricJobLogManager() {
    return getSession(HistoricJobLogManager.class);
  }

  public ReportManager getHistoricReportManager() {
    return getSession(ReportManager.class);
  }

  public AuthorizationManager getAuthorizationManager() {
    return getSession(AuthorizationManager.class);
  }

  public ReadOnlyIdentityProvider getReadOnlyIdentityProvider() {
    return getSession(ReadOnlyIdentityProvider.class);
  }

  public WritableIdentityProvider getWritableIdentityProvider() {
    return getSession(WritableIdentityProvider.class);
  }

  // CMMN /////////////////////////////////////////////////////////////////////

  public CaseDefinitionManager getCaseDefinitionManager() {
    return getSession(CaseDefinitionManager.class);
  }

  public CaseExecutionManager getCaseExecutionManager() {
    return getSession(CaseExecutionManager.class);
  }

  public CaseSentryPartManager getCaseSentryPartManager() {
    return getSession(CaseSentryPartManager.class);
  }

  // DMN //////////////////////////////////////////////////////////////////////

  public DecisionDefinitionManager getDecisionDefinitionManager() {
    return getSession(DecisionDefinitionManager.class);
  }

  public HistoricDecisionInstanceManager getHistoricDecisionInstanceManager() {
    return getSession(HistoricDecisionInstanceManager.class);
  }

  // Filter ////////////////////////////////////////////////////////////////////

  public FilterManager getFilterManager() {
    return getSession(FilterManager.class);
  }

  // External Tasks ////////////////////////////////////////////////////////////

  public ExternalTaskManager getExternalTaskManager() {
    return getSession(ExternalTaskManager.class);
  }

  // getters and setters //////////////////////////////////////////////////////

  public void registerCommandContextListener(CommandContextListener commandContextListener) {
    if(!commandContextListeners.contains(commandContextListener)) {
      commandContextListeners.add(commandContextListener);
    }
  }

  public TransactionContext getTransactionContext() {
    return transactionContext;
  }

  public Map<Class< ? >, Session> getSessions() {
    return sessions;
  }

  public FailedJobCommandFactory getFailedJobCommandFactory() {
    return failedJobCommandFactory;
  }

  public Authentication getAuthentication() {
    IdentityService identityService = processEngineConfiguration.getIdentityService();
    return identityService.getCurrentAuthentication();
  }

  public <T> T runWithoutAuthorization(Callable<T> runnable) {
    CommandContext commandContext = Context.getCommandContext();
    boolean authorizationEnabled = commandContext.isAuthorizationCheckEnabled();
    try {
      commandContext.disableAuthorizationCheck();
      return runnable.call();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new ProcessEngineException(e);
    } finally {
      if (authorizationEnabled) {
        commandContext.enableAuthorizationCheck();
      }
    }
  }

  public String getAuthenticatedUserId() {
    IdentityService identityService = processEngineConfiguration.getIdentityService();
    Authentication currentAuthentication = identityService.getCurrentAuthentication();
    if(currentAuthentication == null) {
      return null;
    } else {
      return currentAuthentication.getUserId();
    }
  }

  public List<String> getAuthenticatedGroupIds() {
    IdentityService identityService = processEngineConfiguration.getIdentityService();
    Authentication currentAuthentication = identityService.getCurrentAuthentication();
    if(currentAuthentication == null) {
      return null;
    } else {
      return currentAuthentication.getGroupIds();
    }
  }

  public void enableAuthorizationCheck() {
    authorizationCheckEnabled = true;
  }

  public void disableAuthorizationCheck() {
    authorizationCheckEnabled = false;
  }

  public boolean isAuthorizationCheckEnabled() {
    return authorizationCheckEnabled;
  }

  public void setAuthorizationCheckEnabled(boolean authorizationCheckEnabled) {
    this.authorizationCheckEnabled = authorizationCheckEnabled;
  }

  public void enableUserOperationLog() {
    userOperationLogEnabled = true;
  }

  public void disableUserOperationLog() {
    userOperationLogEnabled = false;
  }

  public boolean isUserOperationLogEnabled() {
    return userOperationLogEnabled;
  }

  public void setLogUserOperationEnabled(boolean userOperationLogEnabled) {
    this.userOperationLogEnabled = userOperationLogEnabled;
  }
}

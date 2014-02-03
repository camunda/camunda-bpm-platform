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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.ibatis.exceptions.PersistenceException;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskAlreadyClaimedException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.TransactionContext;
import org.camunda.bpm.engine.impl.cfg.TransactionContextFactory;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.ProcessApplicationContextUtil;
import org.camunda.bpm.engine.impl.db.DbSqlSession;
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
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricActivityInstanceManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricProcessInstanceManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricStatisticsManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricTaskInstanceManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceManager;
import org.camunda.bpm.engine.impl.persistence.entity.IdentityInfoManager;
import org.camunda.bpm.engine.impl.persistence.entity.IdentityLinkManager;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentManager;
import org.camunda.bpm.engine.impl.persistence.entity.JobDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.JobManager;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyManager;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceManager;
import org.camunda.bpm.engine.impl.persistence.entity.StatisticsManager;
import org.camunda.bpm.engine.impl.persistence.entity.TableDataManager;
import org.camunda.bpm.engine.impl.persistence.entity.TaskManager;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceManager;
import org.camunda.bpm.engine.impl.pvm.runtime.AtomicOperation;
import org.camunda.bpm.engine.impl.pvm.runtime.InterpretableExecution;

/**
 * @author Tom Baeyens
 * @author Agim Emruli
 * @author Daniel Meyer
 */
public class CommandContext {

  private static Logger log = Logger.getLogger(CommandContext.class.getName());

  protected Command< ? > command;
  protected TransactionContext transactionContext;
  protected Map<Class< ? >, SessionFactory> sessionFactories;
  protected Map<Class< ? >, Session> sessions = new HashMap<Class< ? >, Session>();
  protected Throwable exception = null;
  protected LinkedList<AtomicOperation> nextOperations = new LinkedList<AtomicOperation>();
  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected FailedJobCommandFactory failedJobCommandFactory;

  protected List<CommandContextCloseListener> commandContextCloseListeners = new LinkedList<CommandContextCloseListener>();

  public CommandContext(Command<?> command, ProcessEngineConfigurationImpl processEngineConfiguration) {
    this(command, processEngineConfiguration, processEngineConfiguration.getTransactionContextFactory());
  }

  public CommandContext(Command<?> cmd, ProcessEngineConfigurationImpl processEngineConfiguration, TransactionContextFactory transactionContextFactory) {
    this.command = cmd;
    this.processEngineConfiguration = processEngineConfiguration;
    this.failedJobCommandFactory = processEngineConfiguration.getFailedJobCommandFactory();
    sessionFactories = processEngineConfiguration.getSessionFactories();
    this.transactionContext = transactionContextFactory.openTransactionContext(this);
  }

  public void performOperation(final AtomicOperation executionOperation, final InterpretableExecution execution) {

    ProcessApplicationReference targetProcessApplication = getTargetProcessApplication(execution);

    if(requiresContextSwitch(executionOperation, targetProcessApplication)) {

      Context.executeWithinProcessApplication(new Callable<Void>() {
        public Void call() throws Exception {
          performOperation(executionOperation, execution);
          return null;
        }

      }, targetProcessApplication);

    } else {
      nextOperations.add(executionOperation);
      if (nextOperations.size()==1) {
        try {
          Context.setExecutionContext(execution);
          while (!nextOperations.isEmpty()) {
            AtomicOperation currentOperation = nextOperations.removeFirst();
            if (log.isLoggable(Level.FINEST)) {
              log.finest("AtomicOperation: " + currentOperation + " on " + this);
            }
            currentOperation.execute(execution);
          }
        } finally {
          Context.removeExecutionContext();
        }
      }

    }

  }

  protected ProcessApplicationReference getTargetProcessApplication(InterpretableExecution execution) {

    return ProcessApplicationContextUtil.getTargetProcessApplication(execution);
  }

  protected boolean requiresContextSwitch(final AtomicOperation executionOperation, ProcessApplicationReference processApplicationReference) {

    return ProcessApplicationContextUtil.requiresContextSwitch(processApplicationReference);
  }

  public void close() {
    // the intention of this method is that all resources are closed properly,
    // even
    // if exceptions occur in close or flush methods of the sessions or the
    // transaction context.

    try {
      try {
        try {

          if (exception == null) {
            fireCommandContextClose();
            flushSessions();
          }

        } catch (Throwable exception) {
          exception(exception);
        } finally {

          try {
            if (exception == null) {
              transactionContext.commit();
            }
          } catch (Throwable exception) {
            exception(exception);
          }

          if (exception != null) {
            Level loggingLevel = Level.SEVERE;
            if (exception instanceof TaskAlreadyClaimedException) {
              loggingLevel = Level.INFO; // reduce log level, because this is not really a technical exception
            }
            log.log(loggingLevel, "Error while closing command context", exception);
            transactionContext.rollback();
          }
        }
      } catch (Throwable exception) {
        exception(exception);
      } finally {
        closeSessions();

      }
    } catch (Throwable exception) {
      exception(exception);
    }

    // rethrow the original exception if there was one
    if (exception != null) {
      if (exception instanceof Error) {
        throw (Error) exception;
      } else if (exception instanceof PersistenceException) {
        throw new ProcessEngineException("Process engine persistence exception", exception);
      } else if (exception instanceof RuntimeException) {
        throw (RuntimeException) exception;
      } else {
        throw new ProcessEngineException("exception while executing command " + command, exception);
      }
    }
  }

  protected void fireCommandContextClose() {
    for (CommandContextCloseListener listener : commandContextCloseListeners) {
      listener.onCommandContextClose(this);
    }
  }

  protected void flushSessions() {
    List<Session> sessions = new ArrayList<Session>(this.sessions.values());
    for (Session session : sessions) {
      session.flush();
    }
  }

  protected void closeSessions() {
    List<Session> sessions = new ArrayList<Session>(this.sessions.values());
    for (Session session : sessions) {
      try {
        session.close();
      } catch (Throwable exception) {
        exception(exception);
      }
    }
  }

  public void exception(Throwable exception) {
    if (this.exception == null) {
      this.exception = exception;
    } else {
      log.log(Level.SEVERE, "masked exception in command context. for root cause, see below as it will be rethrown later.", exception);
    }
  }

  @SuppressWarnings({"unchecked"})
  public <T> T getSession(Class<T> sessionClass) {
    Session session = sessions.get(sessionClass);
    if (session == null) {
      SessionFactory sessionFactory = sessionFactories.get(sessionClass);
      if (sessionFactory==null) {
        throw new ProcessEngineException("no session factory configured for "+sessionClass.getName());
      }
      session = sessionFactory.openSession();
      sessions.put(sessionClass, session);
    }

    return (T) session;
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

  public IdentityLinkManager getIdentityLinkManager() {
    return getSession(IdentityLinkManager.class);
  }

  public VariableInstanceManager getVariableInstanceManager() {
    return getSession(VariableInstanceManager.class);
  }

  public HistoricProcessInstanceManager getHistoricProcessInstanceManager() {
    return getSession(HistoricProcessInstanceManager.class);
  }

  public HistoricDetailManager getHistoricDetailManager() {
    return getSession(HistoricDetailManager.class);
  }

  public HistoricVariableInstanceManager getHistoricVariableInstanceManager() {
    return getSession(HistoricVariableInstanceManager.class);
  }

  public HistoricActivityInstanceManager getHistoricActivityInstanceManager() {
    return getSession(HistoricActivityInstanceManager.class);
  }

  public HistoricTaskInstanceManager getHistoricTaskInstanceManager() {
    return getSession(HistoricTaskInstanceManager.class);
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

  public AuthorizationManager getAuthorizationManager() {
    return getSession(AuthorizationManager.class);
  }

  public ReadOnlyIdentityProvider getReadOnlyIdentityProvider() {
    return getSession(ReadOnlyIdentityProvider.class);
  }

  public WritableIdentityProvider getWritableIdentityProvider() {
    return getSession(WritableIdentityProvider.class);
  }

  // getters and setters //////////////////////////////////////////////////////

  public void registerCommandContextCloseListener(CommandContextCloseListener commandContextCloseListener) {
    if(!commandContextCloseListeners.contains(commandContextCloseListener)) {
      commandContextCloseListeners.add(commandContextCloseListener);
    }
  }

  public TransactionContext getTransactionContext() {
    return transactionContext;
  }
  public Command< ? > getCommand() {
    return command;
  }
  public Map<Class< ? >, Session> getSessions() {
    return sessions;
  }
  public Throwable getException() {
    return exception;
  }
  public FailedJobCommandFactory getFailedJobCommandFactory() {
    return failedJobCommandFactory;
  }

  public Authentication getAuthentication() {
    IdentityService identityService = processEngineConfiguration.getIdentityService();
    return identityService.getCurrentAuthentication();
  }

  public void runWithoutAuthentication(Runnable runnable) {
    IdentityService identityService = processEngineConfiguration.getIdentityService();
    Authentication currentAuthentication = identityService.getCurrentAuthentication();
    try {
      identityService.clearAuthentication();
      runnable.run();
    } finally {
      identityService.setAuthentication(currentAuthentication);
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
}

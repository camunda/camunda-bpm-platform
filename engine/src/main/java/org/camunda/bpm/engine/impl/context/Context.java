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

package org.camunda.bpm.engine.impl.context;

import java.util.Stack;
import java.util.concurrent.Callable;

import org.camunda.bpm.application.InvocationContext;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.core.instance.CoreExecution;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandInvocationContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutorContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 * @author Thorben Lindhauer
 */
public class Context {

  protected static ThreadLocal<Stack<CommandContext>> commandContextThreadLocal = new ThreadLocal<Stack<CommandContext>>();
  protected static ThreadLocal<Stack<CommandInvocationContext>> commandInvocationContextThreadLocal = new ThreadLocal<Stack<CommandInvocationContext>>();

  protected static ThreadLocal<Stack<ProcessEngineConfigurationImpl>> processEngineConfigurationStackThreadLocal = new ThreadLocal<Stack<ProcessEngineConfigurationImpl>>();
  protected static ThreadLocal<Stack<CoreExecutionContext<? extends CoreExecution>>> executionContextStackThreadLocal = new ThreadLocal<Stack<CoreExecutionContext<? extends CoreExecution>>>();
  protected static ThreadLocal<JobExecutorContext> jobExecutorContextThreadLocal = new ThreadLocal<JobExecutorContext>();
  protected static ThreadLocal<Stack<ProcessApplicationReference>> processApplicationContext = new ThreadLocal<Stack<ProcessApplicationReference>>();

  public static CommandContext getCommandContext() {
    Stack<CommandContext> stack = getStack(commandContextThreadLocal);
    if (stack.isEmpty()) {
      return null;
    }
    return stack.peek();
  }

  public static void setCommandContext(CommandContext commandContext) {
    getStack(commandContextThreadLocal).push(commandContext);
  }

  public static void removeCommandContext() {
    getStack(commandContextThreadLocal).pop();
  }

  public static CommandInvocationContext getCommandInvocationContext() {
    Stack<CommandInvocationContext> stack = getStack(commandInvocationContextThreadLocal);
    if (stack.isEmpty()) {
      return null;
    }
    return stack.peek();
  }

  public static void setCommandInvocationContext(CommandInvocationContext commandInvocationContext) {
    getStack(commandInvocationContextThreadLocal).push(commandInvocationContext);
  }

  public static void removeCommandInvocationContext() {
    getStack(commandInvocationContextThreadLocal).pop();
  }

  public static ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
    Stack<ProcessEngineConfigurationImpl> stack = getStack(processEngineConfigurationStackThreadLocal);
    if (stack.isEmpty()) {
      return null;
    }
    return stack.peek();
  }

  public static void setProcessEngineConfiguration(ProcessEngineConfigurationImpl processEngineConfiguration) {
    getStack(processEngineConfigurationStackThreadLocal).push(processEngineConfiguration);
  }

  public static void removeProcessEngineConfiguration() {
    getStack(processEngineConfigurationStackThreadLocal).pop();
  }

  /**
   * @deprecated since 7.2, use {@link #getBpmnExecutionContext()}
   */
  @Deprecated
  public static ExecutionContext getExecutionContext() {
    return getBpmnExecutionContext();
  }

  public static BpmnExecutionContext getBpmnExecutionContext() {
    return (BpmnExecutionContext) getCoreExecutionContext();
  }

  public static CaseExecutionContext getCaseExecutionContext() {
    return (CaseExecutionContext) getCoreExecutionContext();
  }

  public static CoreExecutionContext<? extends CoreExecution> getCoreExecutionContext() {
    Stack<CoreExecutionContext<? extends CoreExecution>> stack = getStack(executionContextStackThreadLocal);
    if(stack == null || stack.isEmpty()) {
      return null;
    } else {
      return stack.peek();
    }
  }

  public static void setExecutionContext(ExecutionEntity execution) {
    getStack(executionContextStackThreadLocal).push(new BpmnExecutionContext(execution));
  }

  public static void setExecutionContext(CaseExecutionEntity execution) {
    getStack(executionContextStackThreadLocal).push(new CaseExecutionContext(execution));
  }

  public static void removeExecutionContext() {
    getStack(executionContextStackThreadLocal).pop();
  }

  protected static <T> Stack<T> getStack(ThreadLocal<Stack<T>> threadLocal) {
    Stack<T> stack = threadLocal.get();
    if (stack==null) {
      stack = new Stack<T>();
      threadLocal.set(stack);
    }
    return stack;
  }

  public static JobExecutorContext getJobExecutorContext() {
    return jobExecutorContextThreadLocal.get();
  }

  public static void setJobExecutorContext(JobExecutorContext jobExecutorContext) {
    jobExecutorContextThreadLocal.set(jobExecutorContext);
  }

  public static void removeJobExecutorContext() {
    jobExecutorContextThreadLocal.remove();
  }


  public static ProcessApplicationReference getCurrentProcessApplication() {
    Stack<ProcessApplicationReference> stack = getStack(processApplicationContext);
    if(stack.isEmpty()) {
      return null;
    } else {
      return stack.peek();
    }
  }

  public static void setCurrentProcessApplication(ProcessApplicationReference reference) {
    Stack<ProcessApplicationReference> stack = getStack(processApplicationContext);
    stack.push(reference);
  }

  public static void removeCurrentProcessApplication() {
    Stack<ProcessApplicationReference> stack = getStack(processApplicationContext);
    stack.pop();
  }

  /**
   * Use {@link #executeWithinProcessApplication(Callable, ProcessApplicationReference, InvocationContext)}
   * instead if an {@link InvocationContext} is available.
   */
  public static <T> T executeWithinProcessApplication(Callable<T> callback, ProcessApplicationReference processApplicationReference) {
    return executeWithinProcessApplication(callback, processApplicationReference, null);
  }

  public static <T> T executeWithinProcessApplication(Callable<T> callback, ProcessApplicationReference processApplicationReference, InvocationContext invocationContext) {
    String paName = processApplicationReference.getName();
    try {
      ProcessApplicationInterface processApplication = processApplicationReference.getProcessApplication();
      setCurrentProcessApplication(processApplicationReference);

      try {
        // wrap callback
        ProcessApplicationClassloaderInterceptor<T> wrappedCallback = new ProcessApplicationClassloaderInterceptor<T>(callback);
        // execute wrapped callback
        return processApplication.execute(wrappedCallback, invocationContext);

      } catch (Exception e) {

        // unwrap exception
        if(e.getCause() != null && e.getCause() instanceof RuntimeException) {
          throw (RuntimeException) e.getCause();
        }else {
          throw new ProcessEngineException("Unexpected exeption while executing within process application ", e);
        }

      } finally {
        removeCurrentProcessApplication();
      }


    } catch (ProcessApplicationUnavailableException e) {
      throw new ProcessEngineException("Cannot switch to process application '"+paName+"' for execution: "+e.getMessage(), e);
    }
  }
}

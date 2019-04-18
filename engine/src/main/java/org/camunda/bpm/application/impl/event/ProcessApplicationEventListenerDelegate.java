/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.application.impl.event;

import java.util.concurrent.Callable;

import org.camunda.bpm.application.InvocationContext;
import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;
import org.camunda.bpm.application.impl.ProcessApplicationLogger;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.context.ProcessApplicationContextUtil;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;

/**
 * <p>{@link ExecutionListener} and {@link TaskListener} implementation delegating to
 * the {@link ExecutionListener} and {@link TaskListener} provided by a
 * {@link ProcessApplicationInterface ProcessApplication}.</p>
 *
 * <p>If the process application does not provide an execution listener (ie.
 * {@link ProcessApplicationInterface#getExecutionListener()} returns null), the
 * request is silently ignored.</p>
 *
 * <p>If the process application does not provide a task listener (ie.
 * {@link ProcessApplicationInterface#getTaskListener()} returns null), the
 * request is silently ignored.</p>
 *
 *
 * @author Daniel Meyer
 * @see ProcessApplicationInterface#getExecutionListener()
 * @see ProcessApplicationInterface#getTaskListener()
 *
 */
public class ProcessApplicationEventListenerDelegate implements ExecutionListener, TaskListener {

  private static ProcessApplicationLogger LOG = ProcessApplicationLogger.PROCESS_APPLICATION_LOGGER;

  public void notify(final DelegateExecution execution) throws Exception {
    Callable<Void> notification = new Callable<Void>() {
      public Void call() throws Exception {
        notifyExecutionListener(execution);
        return null;
      }
    };
    performNotification(execution, notification);
  }

  public void notify(final DelegateTask delegateTask){
    if(delegateTask.getExecution() == null) {
      LOG.taskNotRelatedToExecution(delegateTask);
    } else {
      final DelegateExecution execution = delegateTask.getExecution();
      Callable<Void> notification = new Callable<Void>() {
        public Void call() throws Exception {
          notifyTaskListener(delegateTask);
          return null;
        }
      };
      try {
        performNotification(execution, notification);
      } catch(Exception e) {
        throw LOG.exceptionWhileNotifyingPaTaskListener(e);
      }
    }
  }

  protected void performNotification(final DelegateExecution execution, Callable<Void> notification) throws Exception {
    final ProcessApplicationReference processApp = ProcessApplicationContextUtil.getTargetProcessApplication((ExecutionEntity) execution);
    if (processApp == null) {
      // ignore silently
      LOG.noTargetProcessApplicationForExecution(execution);

    } else {
      if (ProcessApplicationContextUtil.requiresContextSwitch(processApp)) {
        // this should not be necessary since context switch is already performed by OperationContext and / or DelegateInterceptor
        Context.executeWithinProcessApplication(notification, processApp, new InvocationContext(execution));

      } else {
        // context switch already performed
        notification.call();

      }
    }
  }

  protected void notifyExecutionListener(DelegateExecution execution) throws Exception {
    ProcessApplicationReference processApp = Context.getCurrentProcessApplication();
    try {
      ProcessApplicationInterface processApplication = processApp.getProcessApplication();
      ExecutionListener executionListener = processApplication.getExecutionListener();
      if(executionListener != null) {
        executionListener.notify(execution);

      } else {
        LOG.paDoesNotProvideExecutionListener(processApp.getName());

      }
    } catch (ProcessApplicationUnavailableException e) {
      // Process Application unavailable => ignore silently
      LOG.cannotInvokeListenerPaUnavailable(processApp.getName(), e);
    }
  }

  protected void notifyTaskListener(DelegateTask task) throws Exception {
    ProcessApplicationReference processApp = Context.getCurrentProcessApplication();
    try {
      ProcessApplicationInterface processApplication = processApp.getProcessApplication();
      TaskListener taskListener = processApplication.getTaskListener();
      if(taskListener != null) {
        taskListener.notify(task);

      } else {
        LOG.paDoesNotProvideTaskListener(processApp.getName());

      }
    } catch (ProcessApplicationUnavailableException e) {
      // Process Application unavailable => ignore silently
      LOG.cannotInvokeListenerPaUnavailable(processApp.getName(), e);
    }
  }

}

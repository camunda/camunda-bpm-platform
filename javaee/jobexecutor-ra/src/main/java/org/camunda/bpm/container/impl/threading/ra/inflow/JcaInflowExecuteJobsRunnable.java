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
package org.camunda.bpm.container.impl.threading.ra.inflow;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.resource.ResourceException;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;

import org.camunda.bpm.container.impl.threading.ra.JcaExecutorServiceConnector;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobsRunnable;
import org.camunda.bpm.engine.impl.jobexecutor.JobFailureCollector;


/**
 *
 * @author Daniel Meyer
 *
 */
public class JcaInflowExecuteJobsRunnable extends ExecuteJobsRunnable {

  private Logger log = Logger.getLogger(JcaInflowExecuteJobsRunnable.class.getName());

  protected final JcaExecutorServiceConnector ra;

  protected static Method method;

  public JcaInflowExecuteJobsRunnable(List<String> jobIds, ProcessEngineImpl processEngine, JcaExecutorServiceConnector connector) {
    super(jobIds, processEngine);
    this.ra = connector;
    if(method == null) {
      loadMethod();
    }
  }

  @Override
  protected void executeJob(String nextJobId, CommandExecutor commandExecutor, JobFailureCollector jobFailureCollector) {
    JobExecutionHandlerActivation jobHandlerActivation = ra.getJobHandlerActivation();
    if(jobHandlerActivation == null) {
      // TODO: stop acquisition / only activate acquisition if MDB active?
      log.warning("Cannot execute acquired job, no JobExecutionHandler MDB deployed.");
      return;
    }
    MessageEndpoint endpoint = null;
    try {
      endpoint = jobHandlerActivation.getMessageEndpointFactory().createEndpoint(null);

      try {
        endpoint.beforeDelivery(method);
      } catch (NoSuchMethodException e) {
        log.log(Level.WARNING, "NoSuchMethodException while invoking beforeDelivery() on MessageEndpoint '"+endpoint+"'", e);
      } catch (ResourceException e) {
        log.log(Level.WARNING, "ResourceException while invoking beforeDelivery() on MessageEndpoint '"+endpoint+"'", e);
      }
      try {
        jobFailureCollector = ((JobExecutionHandler)endpoint).executeJob(nextJobId, commandExecutor);
      }catch (Exception e) {
        if(ProcessEngineLogger.shouldLogJobException(processEngine.getProcessEngineConfiguration(), jobFailureCollector.getJob())) {
          log.log(Level.WARNING, "Exception while executing job with id '"+nextJobId+"'.", e);
        }
      }

      try {
        endpoint.afterDelivery();
      } catch (ResourceException e) {
        log.log(Level.WARNING, "ResourceException while invoking afterDelivery() on MessageEndpoint '"+endpoint+"'", e);
      }

    } catch (UnavailableException e) {
      log.log(Level.SEVERE, "UnavailableException while attempting to create messaging endpoint for executing job", e);
    } finally {
      if(endpoint != null) {
        endpoint.release();
      }
    }
  }

  protected void loadMethod() {
    try {
      method = JobExecutionHandler.class.getMethod("executeJob", new Class[] {String.class, CommandExecutor.class});
    } catch (SecurityException e) {
      throw new RuntimeException("SecurityException while invoking getMethod() on class "+JobExecutionHandler.class, e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("NoSuchMethodException while invoking getMethod() on class "+JobExecutionHandler.class, e);
    }
  }

  /**
   * Context class loader switch is not necessary since
   * the loader used for job execution is successor of the engine's
   * @see org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobsRunnable#switchClassLoader()
   *
   * @return the context class loader of the current thread.
   */
  @Override
  protected ClassLoader switchClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }
}

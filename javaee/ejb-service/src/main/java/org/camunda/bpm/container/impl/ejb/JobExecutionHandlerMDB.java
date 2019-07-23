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
package org.camunda.bpm.container.impl.ejb;

import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.camunda.bpm.container.impl.threading.ra.inflow.JobExecutionHandler;
import org.camunda.bpm.engine.impl.cmd.ExecuteJobsCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.ExecuteJobHelper;
import org.camunda.bpm.engine.impl.jobexecutor.JobFailureCollector;


/**
 * <p>MessageDrivenBean implementation of the {@link JobExecutionHandler} interface</p>
 *
 * @author Daniel Meyer
 */
@MessageDriven(
  name="JobExecutionHandlerMDB",
  messageListenerInterface=JobExecutionHandler.class
)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class JobExecutionHandlerMDB implements JobExecutionHandler {

  public JobFailureCollector executeJob(String job, CommandExecutor commandExecutor) {
    JobFailureCollector jobFailureCollector = new JobFailureCollector(job);
    ExecuteJobHelper.executeJob(job, commandExecutor, jobFailureCollector, new ExecuteJobsCmd(job, jobFailureCollector));
    return jobFailureCollector;
  }

}

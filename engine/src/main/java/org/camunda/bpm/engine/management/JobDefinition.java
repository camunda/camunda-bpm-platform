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
package org.camunda.bpm.engine.management;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * <p>A Job Definition provides details about asynchronous background
 * processing ("Jobs") performed by the process engine.</p>
 *
 * <p>Each Job Definition corresponds to a Timer or Asynchronous continuation
 * job installed in the process engine. Jobs definitions are installed when
 * BPMN 2.0 processes containing timer activities or asynchronous continuations
 * are deployed.</p>
 *
 * @author Daniel Meyer
 *
 */
public interface JobDefinition {

  /**
   * @return the Id of the job definition.
   */
  String getId();

  /**
   * @return the id of the {@link ProcessDefinition} this job definition is associated with.
   */
  String getProcessDefinitionId();

  /**
   * @return the key of the {@link ProcessDefinition} this job definition is associated with.
   */
  String getProcessDefinitionKey();

  /**
   * The Type of a job. Asynchronous continuation, timer, ...
   *
   * @return the type of a Job.
   */
  String getJobType();

  /**
   * The configuration of a job definition provides details about the jobs which will be created.
   * For timer jobs this method returns the timer configuration.
   *
   * @return the configuration of this job definition.
   */
  String getJobConfiguration();

  /**
   * The Id of the activity (from BPMN 2.0 Xml) this Job Definition is associated with.
   *
   * @return the activity id for this Job Definition.
   */
  String getActivityId();


  /**
   * Indicates whether this job definition is suspended. If a job Definition is suspended,
   * No Jobs created form the job definition will be acquired by the job executor.
   *
   * @return true if this Job Definition is currently suspended.
   */
  boolean isSuspended();

  /**
   * <p>Returns the execution priority for jobs of this definition, if it was set using the
   * {@link ManagementService} API. When a job is assigned a priority, the job definition's overriding
   * priority (if set) is used instead of the values defined in the BPMN XML.</p>
   *
   * @return the priority that overrides the default/BPMN XML priority or <code>null</code> if
   *   no overriding priority is set
   *
   * @since 7.4
   */
  Long getOverridingJobPriority();

  /**
   * The id of the tenant this job definition belongs to. Can be <code>null</code>
   * if the definition belongs to no single tenant.
   */
  String getTenantId();

  /**
   * The id of the deployment this job definition is related to. In a deployment-aware setup,
   * this leads to all jobs of the same definition being executed on the same node.
   */
  String getDeploymentId();

}

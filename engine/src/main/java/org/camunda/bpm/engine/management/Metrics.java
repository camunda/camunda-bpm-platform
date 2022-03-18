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

/**
 * @author Daniel Meyer
 *
 */
public class Metrics {

  public static final String ACTIVTY_INSTANCE_START = "activity-instance-start";
  public static final String ACTIVTY_INSTANCE_END = "activity-instance-end";
  public static final String FLOW_NODE_INSTANCES = "flow-node-instances";

  /**
   * Number of times job acquisition is performed
   */
  public static final String JOB_ACQUISITION_ATTEMPT = "job-acquisition-attempt";

  /**
   * Number of jobs successfully acquired (i.e. selected + locked)
   */
  public static final String JOB_ACQUIRED_SUCCESS = "job-acquired-success";
  /**
   * Number of jobs attempted to acquire but with failure (i.e. selected + lock failed)
   */
  public static final String JOB_ACQUIRED_FAILURE = "job-acquired-failure";

  /**
   * Number of jobs that were submitted for execution but were rejected due to
   * resource shortage. In the default job executor, this is the case when
   * the execution queue is full.
   */
  public static final String JOB_EXECUTION_REJECTED = "job-execution-rejected";

  public static final String JOB_SUCCESSFUL = "job-successful";
  public static final String JOB_FAILED = "job-failed";

  /**
   * Number of jobs that are immediately locked and executed because they are exclusive
   * and created in the context of job execution
   */
  public static final String JOB_LOCKED_EXCLUSIVE = "job-locked-exclusive";

  /**
   * Number of executed Root Process Instance executions.
   */
  public static final String ROOT_PROCESS_INSTANCE_START = "root-process-instance-start";
  public static final String PROCESS_INSTANCES = "process-instances";

  /**
   * Number of executed decision elements in the DMN engine.
   */
  public static final String EXECUTED_DECISION_ELEMENTS = "executed-decision-elements";
  public static final String EXECUTED_DECISION_INSTANCES = "executed-decision-instances";
  public static final String DECISION_INSTANCES = "decision-instances";

  /**
   * Number of instances removed by history cleanup.
   */
  public static final String HISTORY_CLEANUP_REMOVED_PROCESS_INSTANCES = "history-cleanup-removed-process-instances";
  public static final String HISTORY_CLEANUP_REMOVED_CASE_INSTANCES = "history-cleanup-removed-case-instances";
  public static final String HISTORY_CLEANUP_REMOVED_DECISION_INSTANCES = "history-cleanup-removed-decision-instances";
  public static final String HISTORY_CLEANUP_REMOVED_BATCH_OPERATIONS = "history-cleanup-removed-batch-operations";
  public static final String HISTORY_CLEANUP_REMOVED_TASK_METRICS = "history-cleanup-removed-task-metrics";

  /**
   * Number of unique task workers
   */
  public static final String UNIQUE_TASK_WORKERS = "unique-task-workers";
  public static final String TASK_USERS = "task-users";
}

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
package org.camunda.bpm.engine.management;

/**
 * @author Daniel Meyer
 *
 */
public class Metrics {

  public final static String ACTIVTY_INSTANCE_START = "activity-instance-start";
  public final static String ACTIVTY_INSTANCE_END = "activity-instance-end";

  /**
   * Number of times job acqusition is performed
   */
  public final static String JOB_ACQUISITION_ATTEMPT = "job-acquisition-attempt";

  /**
   * Number of jobs successfully acquired (i.e. selected + locked)
   */
  public final static String JOB_ACQUIRED_SUCCESS = "job-acquired-success";
  /**
   * Number of jobs attempted to acquire but with failure (i.e. selected + lock failed)
   */
  public final static String JOB_ACQUIRED_FAILURE = "job-acquired-failure";

  /**
   * Number of jobs that were submitted for execution but were rejected due to
   * resource shortage. In the default job executor, this is the case when
   * the execution queue is full.
   */
  public final static String JOB_EXECUTION_REJECTED = "job-execution-rejected";

  public final static String JOB_SUCCESSFUL = "job-successful";
  public final static String JOB_FAILED = "job-failed";

  /**
   * Number of jobs that are immediately locked and executed because they are exclusive
   * and created in the context of job execution
   */
  public final static String JOB_LOCKED_EXCLUSIVE = "job-locked-exclusive";

  /**
   * Number of executed decision elements in the DMN engine.
   */
  public final static String EXECUTED_DECISION_ELEMENTS = "executed-decision-elements";

  /**
   * Number of instances removed by history cleanup.
   */
  public final static String HISTORY_CLEANUP_REMOVED_PROCESS_INSTANCES = "history-cleanup-removed-process-instances";
  public final static String HISTORY_CLEANUP_REMOVED_CASE_INSTANCES = "history-cleanup-removed-case-instances";
  public final static String HISTORY_CLEANUP_REMOVED_DECISION_INSTANCES = "history-cleanup-removed-decision-instances";
  public final static String HISTORY_CLEANUP_REMOVED_BATCH_OPERATIONS = "history-cleanup-removed-batch-operations";
}

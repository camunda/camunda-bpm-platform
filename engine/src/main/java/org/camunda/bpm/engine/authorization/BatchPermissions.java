/*
 * Copyright © 2013-2019 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.authorization;

public enum BatchPermissions implements Permission {

  /** Indicates that CREATE_BATCH_MIGRATE_PROCESS_INSTANCES interactions are permitted. */
  CREATE_BATCH_MIGRATE_PROCESS_INSTANCES("CREATE_BATCH_MIGRATE_PROCESS_INSTANCES", 32),

  /** Indicates that CREATE_BATCH_MODIFY_PROCESS_INSTANCES interactions are permitted */
  CREATE_BATCH_MODIFY_PROCESS_INSTANCES("CREATE_BATCH_MODIFY_PROCESS_INSTANCES", 64),

  /** Indicates that CREATE_BATCH_RESTART_PROCESS_INSTANCES interactions are permitted */
  CREATE_BATCH_RESTART_PROCESS_INSTANCES("CREATE_BATCH_RESTART_PROCESS_INSTANCES", 128),

  /** Indicates that CREATE_BATCH_DELETE_RUNNING_PROCESS_INSTANCES interactions are permitted */
  CREATE_BATCH_DELETE_RUNNING_PROCESS_INSTANCES("CREATE_BATCH_DELETE_RUNNING_PROCESS_INSTANCES", 256),

  /** Indicates that CREATE_BATCH_DELETE_FINISHED_PROCESS_INSTANCES interactions are permitted. */
  CREATE_BATCH_DELETE_FINISHED_PROCESS_INSTANCES("CREATE_BATCH_DELETE_FINISHED_PROCESS_INSTANCES", 512),

  /** Indicates that CREATE_BATCH_DELETE_DECISION_INSTANCES interactions are permitted */
  CREATE_BATCH_DELETE_DECISION_INSTANCES("CREATE_BATCH_DELETE_DECISION_INSTANCES", 1024),

  /** Indicates that CREATE_BATCH_SET_JOB_RETRIES interactions are permitted */
  CREATE_BATCH_SET_JOB_RETRIES("CREATE_BATCH_SET_JOB_RETRIES", 2048),

  /** Indicates that CREATE_BATCH_SET_EXTERNAL_TASK_RETRIES interactions are permitted */
  CREATE_BATCH_SET_EXTERNAL_TASK_RETRIES("CREATE_BATCH_SET_EXTERNAL_TASK_RETRIES", 16384),

  /** Indicates that CREATE_BATCH_SUSPEND_PROCESS_INSTANCES interactions are permitted */
  CREATE_BATCH_UPDATE_PROCESS_INSTANCES_SUSPEND_STATE("CREATE_BATCH_UPDATE_PROCESS_INSTANCES_SUSPEND_STATE", 32768);

  private String name;
  private int id;

  private BatchPermissions(String name, int id) {
    this.name = name;
    this.id = id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int getValue() {
    return id;
  }

}

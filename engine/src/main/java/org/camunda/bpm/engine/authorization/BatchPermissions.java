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
package org.camunda.bpm.engine.authorization;

/**
 * The set of built-in {@link Permission Permissions} for {@link Resources#BATCH Batch operations} in Camunda BPM.
 *
 * @author Yana Vasileva
 *
 */
public enum BatchPermissions implements Permission {

  /** The none permission means 'no action', 'doing nothing'.
   * It does not mean that no permissions are granted. */
  NONE("NONE", 0),

  /**
   * Indicates that  all interactions are permitted.
   * If ALL is revoked it means that the user is not permitted
   * to do everything, which means that at least one permission
   * is revoked. This does not implicate that all individual
   * permissions are revoked.
   *
   * Example: If the UPDATE permission is revoke also the ALL
   * permission is revoked, because the user is not authorized
   * to execute all actions anymore.
   */
  ALL("ALL", Integer.MAX_VALUE),

  /** Indicates that READ interactions are permitted. */
  READ("READ", 2),

  /** Indicates that UPDATE interactions are permitted. */
  UPDATE("UPDATE", 4),

  /** Indicates that CREATE interactions are permitted. */
  CREATE("CREATE", 8),

  /** Indicates that DELETE interactions are permitted. */
  DELETE("DELETE", 16),

  /** Indicates that READ_HISTORY interactions are permitted. */
  READ_HISTORY("READ_HISTORY", 4096),

  /** Indicates that DELETE_HISTORY interactions are permitted. */
  DELETE_HISTORY("DELETE_HISTORY", 8192),

  // Create Batch specific permissions: //////////////////////

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

  /** Indicates that CREATE_BATCH_UPDATE_PROCESS_INSTANCES_SUSPEND interactions are permitted */
  CREATE_BATCH_UPDATE_PROCESS_INSTANCES_SUSPEND("CREATE_BATCH_UPDATE_PROCESS_INSTANCES_SUSPEND", 32768),

  /** Indicates that CREATE_BATCH_SET_REMOVAL_TIME interactions are permitted */
  CREATE_BATCH_SET_REMOVAL_TIME("CREATE_BATCH_SET_REMOVAL_TIME", 65536),

  /** Indicates that CREATE_BATCH_SET_VARIABLES interactions are permitted */
  CREATE_BATCH_SET_VARIABLES("CREATE_BATCH_SET_VARIABLES", 131_072);

  protected static final Resource[] RESOURCES = new Resource[] { Resources.BATCH };

  protected String name;
  protected int id;

  BatchPermissions(String name, int id) {
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

  @Override
  public Resource[] getTypes() {
    return RESOURCES;
  }

  public static Permission forName(String name) {
    Permission permission = valueOf(name);
    return permission;
  }
}

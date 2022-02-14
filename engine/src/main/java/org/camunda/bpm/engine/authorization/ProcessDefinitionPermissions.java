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
 * The set of built-in {@link Permission Permissions} for {@link Resources#PROCESS_DEFINITION Process definition} in Camunda Platform.
 *
 * @author Yana Vasileva
 *
 */
public enum ProcessDefinitionPermissions implements Permission {

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

  /** Indicates that DELETE interactions are permitted. */
  DELETE("DELETE", 16),

  /** Indicates that READ_TASK interactions are permitted. */
  READ_TASK("READ_TASK", 64),

  /** Indicates that UPDATE_TASK interactions are permitted. */
  UPDATE_TASK("UPDATE_TASK", 128),

  /** Indicates that CREATE_INSTANCE interactions are permitted. */
  CREATE_INSTANCE("CREATE_INSTANCE", 256),

  /** Indicates that READ_INSTANCE interactions are permitted. */
  READ_INSTANCE("READ_INSTANCE", 512),

  /** Indicates that UPDATE_INSTANCE interactions are permitted. */
  UPDATE_INSTANCE("UPDATE_INSTANCE", 1024),

  /** Indicates that DELETE_INSTANCE interactions are permitted. */
  DELETE_INSTANCE("DELETE_INSTANCE", 2048),

  /** Indicates that READ_HISTORY interactions are permitted. */
  READ_HISTORY("READ_HISTORY", 4096),

  /** Indicates that DELETE_HISTORY interactions are permitted. */
  DELETE_HISTORY("DELETE_HISTORY", 8192),

  /** Indicates that TASK_WORK interactions are permitted */
  TASK_WORK("TASK_WORK", 16384),

  /** Indicates that TASK_ASSIGN interactions are permitted */
  TASK_ASSIGN("TASK_ASSIGN", 32768),

  /** Indicates that MIGRATE_INSTANCE interactions are permitted */
  MIGRATE_INSTANCE("MIGRATE_INSTANCE", 65536),

  /** Indicates that RETRY_JOB interactions are permitted. */
  RETRY_JOB("RETRY_JOB", 32),

  /** Indicates that SUSPEND interactions are permitted. */
  SUSPEND("SUSPEND", 1048576),

  /** Indicates that SUSPEND_INSTANCE interactions are permitted. */
  SUSPEND_INSTANCE("SUSPEND_INSTANCE", 131072),

  /** Indicates that UPDATE_INSTANCE_VARIABLE interactions are permitted. */
  UPDATE_INSTANCE_VARIABLE("UPDATE_INSTANCE_VARIABLE", 262144),

  /** Indicates that UPDATE_TASK_VARIABLE interactions are permitted. */
  UPDATE_TASK_VARIABLE("UPDATE_TASK_VARIABLE", 524288),

  /** Indicates that READ_INSTANCE_VARIABLE interactions are permitted. */
  READ_INSTANCE_VARIABLE("READ_INSTANCE_VARIABLE", 2097152),

  /** Indicates that READ_HISTORY_VARIABLE interactions are permitted. */
  READ_HISTORY_VARIABLE("READ_HISTORY_VARIABLE", 4194304),

  /** Indicates that READ_TASK_VARIABLE interactions are permitted. */
  READ_TASK_VARIABLE("READ_TASK_VARIABLE", 8388608),

  /** Indicates that UPDATE_HISTORY interactions are permitted. */
  UPDATE_HISTORY("UPDATE_HISTORY", 16_777_216);

  private static final Resource[] RESOURCES = new Resource[] { Resources.PROCESS_DEFINITION };

  private String name;
  private int id;

  private ProcessDefinitionPermissions(String name, int id) {
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

}

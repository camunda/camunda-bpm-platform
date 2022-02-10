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

import java.util.EnumSet;

/**
 * The set of built-in {@link Permission Permissions} for Camunda Platform.
 *
 * @author Daniel Meyer
 *
 */
public enum Permissions implements Permission {

  /** The none permission means 'no action', 'doing nothing'.
   * It does not mean that no permissions are granted. */
  NONE("NONE", 0, EnumSet.allOf(Resources.class)),

  /**
   * Indicates that  all interactions are permitted.
   * If ALL is revoked it means that the user is not permitted
   * to do everything, which means that at least one permission
   * is revoked. This does not implicate that all individual
   * permissions are revoked.
   *
   * Example: If the UPDATE permission is revoked then the ALL
   * permission is revoked as well, because the user is not authorized
   * to execute all actions anymore.
   */
  ALL("ALL", Integer.MAX_VALUE, EnumSet.allOf(Resources.class)),

  /** Indicates that READ interactions are permitted. */
  READ("READ", 2,
      EnumSet.of(Resources.AUTHORIZATION, Resources.BATCH, Resources.DASHBOARD, Resources.DECISION_DEFINITION, Resources.DECISION_REQUIREMENTS_DEFINITION,
          Resources.DEPLOYMENT, Resources.FILTER, Resources.GROUP, Resources.PROCESS_DEFINITION, Resources.PROCESS_INSTANCE, Resources.REPORT, Resources.TASK,
          Resources.TENANT, Resources.USER)),

  /** Indicates that UPDATE interactions are permitted. */
  UPDATE("UPDATE", 4, EnumSet.of(Resources.AUTHORIZATION, Resources.BATCH, Resources.DASHBOARD, Resources.DECISION_DEFINITION, Resources.FILTER,
      Resources.GROUP, Resources.PROCESS_DEFINITION, Resources.PROCESS_INSTANCE, Resources.REPORT, Resources.TASK, Resources.TENANT, Resources.USER)),

  /** Indicates that CREATE interactions are permitted. */
  CREATE("CREATE", 8,
      EnumSet.of(Resources.AUTHORIZATION, Resources.BATCH, Resources.DASHBOARD, Resources.DEPLOYMENT, Resources.FILTER, Resources.GROUP,
          Resources.GROUP_MEMBERSHIP, Resources.PROCESS_INSTANCE, Resources.REPORT, Resources.TASK, Resources.TENANT, Resources.TENANT_MEMBERSHIP,
          Resources.USER)),

  /** Indicates that DELETE interactions are permitted. */
  DELETE("DELETE", 16,
      EnumSet.of(Resources.AUTHORIZATION, Resources.BATCH, Resources.DASHBOARD, Resources.DEPLOYMENT, Resources.FILTER, Resources.GROUP,
          Resources.GROUP_MEMBERSHIP, Resources.PROCESS_DEFINITION, Resources.PROCESS_INSTANCE, Resources.REPORT, Resources.TASK, Resources.TENANT,
          Resources.TENANT_MEMBERSHIP, Resources.USER)),

  /** Indicates that ACCESS interactions are permitted. */
  ACCESS("ACCESS", 32, EnumSet.of(Resources.APPLICATION)),

  /** Indicates that READ_TASK interactions are permitted. */
  READ_TASK("READ_TASK", 64, EnumSet.of(Resources.PROCESS_DEFINITION)),

  /** Indicates that UPDATE_TASK interactions are permitted. */
  UPDATE_TASK("UPDATE_TASK", 128, EnumSet.of(Resources.PROCESS_DEFINITION)),

  /** Indicates that CREATE_INSTANCE interactions are permitted. */
  CREATE_INSTANCE("CREATE_INSTANCE", 256, EnumSet.of(Resources.DECISION_DEFINITION, Resources.PROCESS_DEFINITION)),

  /** Indicates that READ_INSTANCE interactions are permitted. */
  READ_INSTANCE("READ_INSTANCE", 512, EnumSet.of(Resources.PROCESS_DEFINITION)),

  /** Indicates that UPDATE_INSTANCE interactions are permitted. */
  UPDATE_INSTANCE("UPDATE_INSTANCE", 1024, EnumSet.of(Resources.PROCESS_DEFINITION)),

  /** Indicates that DELETE_INSTANCE interactions are permitted. */
  DELETE_INSTANCE("DELETE_INSTANCE", 2048, EnumSet.of(Resources.PROCESS_DEFINITION)),

  /**
   * <p>Indicates that READ_HISTORY interactions are permitted.
   *
   * <p>There is no built-in functionality of Camunda Platform that uses the permission together
   * with the resource {@link Resources#TASK}
   * */
  READ_HISTORY("READ_HISTORY", 4096, EnumSet.of(Resources.BATCH, Resources.DECISION_DEFINITION, Resources.PROCESS_DEFINITION, Resources.TASK)),

  /** Indicates that DELETE_HISTORY interactions are permitted. */
  DELETE_HISTORY("DELETE_HISTORY", 8192, EnumSet.of(Resources.BATCH, Resources.DECISION_DEFINITION, Resources.PROCESS_DEFINITION)),

  /** Indicates that TASK_WORK interactions are permitted */
  TASK_WORK("TASK_WORK", 16384, EnumSet.of(Resources.PROCESS_DEFINITION, Resources.TASK)),

  /** Indicates that TASK_ASSIGN interactions are permitted */
  TASK_ASSIGN("TASK_ASSIGN", 32768, EnumSet.of(Resources.PROCESS_DEFINITION, Resources.TASK)),

  /** Indicates that MIGRATE_INSTANCE interactions are permitted */
  MIGRATE_INSTANCE("MIGRATE_INSTANCE", 65536, EnumSet.of(Resources.PROCESS_DEFINITION));

  // NOTE: Please use XxxPermissions for new permissions
  // Keep in mind to use unique permissions' ids for the same Resource
  // TODO in case a new XxxPermissions enum is created:
  // please adjust ResourceTypeUtil#PERMISSION_ENUMS accordingly


  // implementation //////////////////////////

  private String name;
  private int id;
  private Resource[] resourceTypes;

  private Permissions(String name, int id) {
    this.name = name;
    this.id = id;
  }

  private Permissions(String name, int id, EnumSet<Resources> resourceTypes) {
    this.name = name;
    this.id = id;
    this.resourceTypes = resourceTypes.toArray(new Resource[resourceTypes.size()]);
  }

  @Override
  public String toString() {
    return name;
  }

  public String getName() {
    return name;
  }

  public int getValue() {
    return id;
  }

  public Resource[] getTypes() {
    return resourceTypes;
  }

  public static Permission forName(String name) {
    Permission permission = valueOf(name);
    return permission;
  }

}

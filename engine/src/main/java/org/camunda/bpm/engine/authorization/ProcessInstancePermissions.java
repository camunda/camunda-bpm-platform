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
 * The set of built-in {@link Permission Permissions} for {@link Resources#PROCESS_INSTANCE Process instances} in Camunda Platform.
 *
 * @author Yana Vasileva
 *
 */
public enum ProcessInstancePermissions implements Permission {

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

  /** Indicates that RETRY_JOB interactions are permitted. */
  RETRY_JOB("RETRY_JOB", 32),

  /** Indicates that SUSPEND interactions are permitted. */
  SUSPEND("SUSPEND", 64),

  /** Indicates that UPDATE_VARIABLE interactions are permitted. */
  UPDATE_VARIABLE("UPDATE_VARIABLE", 128);

  private static final Resource[] RESOURCES = new Resource[] { Resources.PROCESS_INSTANCE };
  private String name;
  private int id;

  private ProcessInstancePermissions(String name, int id) {
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

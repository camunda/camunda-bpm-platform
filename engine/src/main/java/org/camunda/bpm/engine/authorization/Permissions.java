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
package org.camunda.bpm.engine.authorization;

/**
 * The set of built-in {@link Permission Permissions} for camunda BPM.
 *
 * @author Daniel Meyer
 *
 */
public enum Permissions implements Permission {

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

  /** Indicates that ACCESS interactions are permitted. */
  ACCESS("ACCESS", 32),

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

  /** Indicates that UPDATE_INSTANCE interactions are permitted. */
  READ_HISTORY("READ_HISTORY", 4096),

  /** Indicates that DELETE_INSTANCE interactions are permitted. */
  DELETE_HISTORY("DELETE_HISTORY", 8192),

  /** Indicates that TASK_WORK interactions are permitted */
  TASK_WORK("TASK_WORK", 16384),

  /** Indicates that TASK_ASSIGN interactions are permitted */
  TASK_ASSIGN("TASK_ASSIGN", 32768),

  /** Indicates that MIGRATE_INSTANCE interactions are permitted */
  MIGRATE_INSTANCE("MIGRATE_INSTANCE", 65536);

  // implmentation //////////////////////////

  private String name;
  private int id;

  private Permissions(String name, int id) {
    this.name = name;
    this.id = id;
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

  public static Permission forName(String name) {
    Permission permission = valueOf(name);
    return permission;
  }

}

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
  
  /** Indicates that  all interactions are permitted. */
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
  ACCESS("ACCESS", 32);  
  
  // 10 additional (32 ... 16384(=2^14)) are reserved
  
  // implmentation //////////////////////////
  
  private String name;
  private int id;
  
  private Permissions(String name, int id) {
    this.name = name;
    this.id = id;
  }
  
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
    return valueOf(name);
  }
  
}

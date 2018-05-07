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

import org.camunda.bpm.engine.EntityTypes;

/**
 * <p>The set of built-in {@link Resource} names.</p>
 *
 * @author Daniel Meyer
 *
 */
public enum Resources implements Resource {

  APPLICATION(EntityTypes.APPLICATION, 0),
  USER(EntityTypes.USER, 1),
  GROUP(EntityTypes.GROUP, 2),
  GROUP_MEMBERSHIP(EntityTypes.GROUP_MEMBERSHIP, 3),
  AUTHORIZATION(EntityTypes.AUTHORIZATION, 4),
  FILTER(EntityTypes.FILTER, 5),
  PROCESS_DEFINITION(EntityTypes.PROCESS_DEFINITION, 6),
  TASK(EntityTypes.TASK, 7),
  PROCESS_INSTANCE(EntityTypes.PROCESS_INSTANCE, 8),
  DEPLOYMENT(EntityTypes.DEPLOYMENT, 9),
  DECISION_DEFINITION(EntityTypes.DECISION_DEFINITION, 10),
  TENANT(EntityTypes.TENANT, 11),
  TENANT_MEMBERSHIP(EntityTypes.TENANT_MEMBERSHIP, 12),
  BATCH(EntityTypes.BATCH, 13),
  DECISION_REQUIREMENTS_DEFINITION(EntityTypes.DECISION_REQUIREMENTS_DEFINITION, 14),
  REPORT(EntityTypes.REPORT, 15),
  DASHBOARD(EntityTypes.DASHBOARD, 16);

  String name;
  int id;

  Resources(String name, int id) {
    this.name = name;
    this.id = id;
  }

  public String resourceName() {
    return name;
  }

  public int resourceType() {
    return id;
  }

}

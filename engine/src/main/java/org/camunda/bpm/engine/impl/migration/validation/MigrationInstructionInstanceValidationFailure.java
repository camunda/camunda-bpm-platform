/* Licensed under the Apache License, Version 2.0 (the "License");
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

package org.camunda.bpm.engine.impl.migration.validation;

import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;

public class MigrationInstructionInstanceValidationFailure {

  protected MigratingActivityInstance migratingInstance;
  protected String errorMessage;

  public MigrationInstructionInstanceValidationFailure(MigratingActivityInstance migratingInstance, String errorMessage) {
    this.migratingInstance = migratingInstance;
    this.errorMessage = errorMessage;
  }

  public MigratingActivityInstance getMigratingInstance() {
    return migratingInstance;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void writeTo(StringBuilder sb) {
    sb.append("ActivityInstance ");
    sb.append(migratingInstance.getActivityInstance().getId());
    sb.append(": ");
    sb.append(errorMessage);
  }

}

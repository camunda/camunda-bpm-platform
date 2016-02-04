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

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.migration.MigrationInstructionInstanceValidationFailure;

public class MigrationInstructionInstanceValidationFailureImpl implements MigrationInstructionInstanceValidationFailure {

  protected MigratingActivityInstance migratingInstance;
  protected String errorMessage;

  public MigrationInstructionInstanceValidationFailureImpl(MigratingActivityInstance migratingInstance, String errorMessage) {
    this.migratingInstance = migratingInstance;
    this.errorMessage = errorMessage;
  }

  public MigratingActivityInstance getMigratingInstance() {
    return migratingInstance;
  }

  @Override
  public String getErrorMessage() {
    return errorMessage;
  }

  public void writeTo(StringBuilder sb) {
    sb.append("ActivityInstance ");
    sb.append(migratingInstance.getActivityInstance().getId());
    sb.append(": ");
    sb.append(errorMessage);
  }

  @Override
  public List<String> getActivityInstanceIds() {
    return Arrays.asList(migratingInstance.getActivityInstance().getId());
  }

  @Override
  public MigrationInstruction getMigrationInstruction() {
    return migratingInstance.getMigrationInstruction();
  }

}

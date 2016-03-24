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

package org.camunda.bpm.engine.impl.migration.validation.instance;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.migration.instance.MigratingTransitionInstance;
import org.camunda.bpm.engine.migration.MigratingTransitionInstanceValidationReport;
import org.camunda.bpm.engine.migration.MigrationInstruction;

public class MigratingTransitionInstanceValidationReportImpl implements MigratingTransitionInstanceValidationReport {

  protected String transitionInstanceId;
  protected String sourceScopeId;
  protected MigrationInstruction migrationInstruction;
  protected List<String> failures = new ArrayList<String>();

  public MigratingTransitionInstanceValidationReportImpl(MigratingTransitionInstance migratingTransitionInstance) {
    this.transitionInstanceId = migratingTransitionInstance.getTransitionInstance().getId();
    this.sourceScopeId = migratingTransitionInstance.getSourceScope().getId();
    this.migrationInstruction = migratingTransitionInstance.getMigrationInstruction();
  }

  public String getSourceScopeId() {
    return sourceScopeId;
  }

  public String getTransitionInstanceId() {
    return transitionInstanceId;
  }

  public MigrationInstruction getMigrationInstruction() {
    return migrationInstruction;
  }

  public void addFailure(String failure) {
    failures.add(failure);
  }

  public boolean hasFailures() {
    return !failures.isEmpty();
  }

  public List<String> getFailures() {
    return failures;
  }

}

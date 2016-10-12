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

package org.camunda.bpm.engine.impl.migration.batch;

import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.migration.MigrationPlan;

import java.util.List;

public class MigrationBatchConfiguration extends BatchConfiguration {

  protected MigrationPlan migrationPlan;
  protected boolean isSkipCustomListeners;
  protected boolean isSkipIoMappings;

  public MigrationBatchConfiguration(List<String> ids) {
    super(ids);
  }

  public MigrationBatchConfiguration(List<String> ids,
                                     MigrationPlan migrationPlan,
                                     boolean isSkipCustomListeners,
                                     boolean isSkipIoMappings) {
    super(ids);
    this.migrationPlan = migrationPlan;
    this.isSkipCustomListeners = isSkipCustomListeners;
    this.isSkipIoMappings = isSkipIoMappings;
  }

  public MigrationPlan getMigrationPlan() {
    return migrationPlan;
  }

  public void setMigrationPlan(MigrationPlan migrationPlan) {
    this.migrationPlan = migrationPlan;
  }

  public boolean isSkipCustomListeners() {
    return isSkipCustomListeners;
  }

  public void setSkipCustomListeners(boolean isSkipCustomListeners) {
    this.isSkipCustomListeners = isSkipCustomListeners;
  }

  public boolean isSkipIoMappings() {
    return isSkipIoMappings;
  }

  public void setSkipIoMappings(boolean isSkipIoMappings) {
    this.isSkipIoMappings = isSkipIoMappings;
  }


}

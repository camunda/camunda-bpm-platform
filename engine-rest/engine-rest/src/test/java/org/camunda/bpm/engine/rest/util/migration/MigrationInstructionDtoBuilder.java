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

package org.camunda.bpm.engine.rest.util.migration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MigrationInstructionDtoBuilder {

  public static final String PROP_SOURCE_ACTIVITY_IDS = "sourceActivityIds";
  public static final String PROP_TARGET_ACTIVITY_IDS = "targetActivityIds";
  public static final String PROP_UPDATE_EVENT_TRIGGER = "updateEventTrigger";

  protected final Map<String, Object> migrationInstruction;

  public MigrationInstructionDtoBuilder() {
    migrationInstruction = new HashMap<String, Object>();
  }

  public MigrationInstructionDtoBuilder migrate(String sourceActivityId, String targetActivityId) {
    return migrate(Collections.singletonList(sourceActivityId), Collections.singletonList(targetActivityId), null);
  }

  public MigrationInstructionDtoBuilder migrate(String sourceActivityId, String targetActivityId, Boolean updateEventTrigger) {
    return migrate(Collections.singletonList(sourceActivityId), Collections.singletonList(targetActivityId), updateEventTrigger);
  }

  public MigrationInstructionDtoBuilder migrate(List<String> sourceActivityId, List<String> targetActivityId, Boolean updateEventTrigger) {
    migrationInstruction.put(PROP_SOURCE_ACTIVITY_IDS, sourceActivityId);
    migrationInstruction.put(PROP_TARGET_ACTIVITY_IDS, targetActivityId);
    migrationInstruction.put(PROP_UPDATE_EVENT_TRIGGER, updateEventTrigger);

    return this;
  }

  public Map<String, Object> build() {
    return migrationInstruction;
  }

}

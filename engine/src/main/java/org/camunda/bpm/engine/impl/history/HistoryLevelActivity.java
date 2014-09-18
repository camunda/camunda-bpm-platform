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
package org.camunda.bpm.engine.impl.history;

import org.camunda.bpm.engine.impl.history.event.HistoryEventType;

import static org.camunda.bpm.engine.impl.history.event.HistoryEventTypes.*;

/**
 * @author Daniel Meyer
 *
 */
public class HistoryLevelActivity extends AbstractHistoryLevel {

  public int getId() {
    return 1;
  }

  public String getName() {
    return "activity";
  }

  public boolean isHistoryEventProduced(HistoryEventType eventType, Object entity) {
    return PROCESS_INSTANCE_START == eventType
        || PROCESS_INSTANCE_END == eventType

        || TASK_INSTANCE_CREATE == eventType
        || TASK_INSTANCE_UPDATE == eventType
        || TASK_INSTANCE_COMPLETE == eventType
        || TASK_INSTANCE_DELETE == eventType

        || ACTIVITY_INSTANCE_START == eventType
        || ACTIVITY_INSTANCE_UPDATE == eventType
        || ACTIVITY_INSTANCE_END == eventType
    ;
  }

}

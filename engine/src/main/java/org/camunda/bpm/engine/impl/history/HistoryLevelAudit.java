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
public class HistoryLevelAudit extends HistoryLevelActivity {

  public int getId() {
    return 2;
  }

  public String getName() {
    return "audit";
  }

  @Override
  public boolean isHistoryEventProduced(HistoryEventType eventType, Object entity) {
    return super.isHistoryEventProduced(eventType, entity)
        || VARIABLE_INSTANCE_CREATE == eventType
        || VARIABLE_INSTANCE_UPDATE == eventType
        || VARIABLE_INSTANCE_DELTE == eventType

        || FORM_PROPERTY_UPDATE == eventType
      ;

  }

}

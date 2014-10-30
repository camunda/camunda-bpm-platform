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

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.history.event.HistoryEventType;

/**
 *
 * @author Daniel Meyer
 *
 */
public class HistoryLevelNone extends AbstractHistoryLevel {

  public int getId() {
    return 0;
  }

  public String getName() {
    return ProcessEngineConfiguration.HISTORY_NONE;
  }

  public boolean isHistoryEventProduced(HistoryEventType eventType, Object entity) {
    return false;
  }

}

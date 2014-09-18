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
package org.camunda.bpm.engine.impl.history.event;

import java.io.Serializable;

/**
 * An history event type.
 *
 * See {@link HistoryEventTypes} for a set of built-in events
 *
 * @author Daniel Meyer
 * @since 7.2
 */
public interface HistoryEventType extends Serializable {

  /**
   * The type of the entity.
   */
  String getEntityType();

  /**
   * The name of the event fired on the entity
   */
  String getEventName();

}

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

package org.camunda.bpm.engine.impl.bpmn.helper;

import org.camunda.bpm.engine.impl.bpmn.parser.ErrorEventDefinition;
import org.camunda.bpm.engine.impl.bpmn.parser.EscalationEventDefinition;
import org.camunda.bpm.engine.impl.core.model.Properties;
import org.camunda.bpm.engine.impl.core.model.PropertyKey;
import org.camunda.bpm.engine.impl.core.model.PropertyListKey;

/**
 * Properties of bpmn elements.
 *
 * @author Philipp Ossler
 *
 * @see Properties
 *
 */
public class BpmnProperties {

  public static final PropertyKey<String> TYPE = new PropertyKey<String>("type");

  public static final PropertyListKey<EscalationEventDefinition> ESCALATION_EVENT_DEFINITIONS = new PropertyListKey<EscalationEventDefinition>("escalationEventDefinitions");

  public static final PropertyListKey<ErrorEventDefinition> ERROR_EVENT_DEFINITIONS = new PropertyListKey<ErrorEventDefinition>("errorEventDefinitions");

}

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
import org.camunda.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.camunda.bpm.engine.impl.core.model.Properties;
import org.camunda.bpm.engine.impl.core.model.PropertyKey;
import org.camunda.bpm.engine.impl.core.model.PropertyListKey;
import org.camunda.bpm.engine.impl.core.model.PropertyMapKey;
import org.camunda.bpm.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

import static org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse.PROPERTYNAME_HAS_CONDITIONAL_EVENTS;

import org.camunda.bpm.engine.impl.bpmn.parser.ConditionalEventDefinition;

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

  /**
   * Declaration indexed by activity that is triggered by the event; assumes that there is at most one such declaration per activity.
   * There is code that relies on this assumption (e.g. when determining which declaration matches a job in the migration logic).
   */
  public static final PropertyMapKey<String, TimerDeclarationImpl> TIMER_DECLARATIONS = new PropertyMapKey<String, TimerDeclarationImpl>("timerDeclarations", false);

  /**
   * Declaration indexed by activity that is triggered by the event; assumes that there is at most one such declaration per activity.
   * There is code that relies on this assumption (e.g. when determining which declaration matches a job in the migration logic).
   */
  public static final PropertyMapKey<String, EventSubscriptionDeclaration> EVENT_SUBSCRIPTION_DECLARATIONS = new PropertyMapKey<String, EventSubscriptionDeclaration>("eventDefinitions", false);

  public static final PropertyKey<ActivityImpl> COMPENSATION_BOUNDARY_EVENT = new PropertyKey<ActivityImpl>("compensationBoundaryEvent");

  public static final PropertyKey<ActivityImpl> INITIAL_ACTIVITY = new PropertyKey<ActivityImpl>("initial");

  public static final PropertyKey<Boolean> TRIGGERED_BY_EVENT = new PropertyKey<Boolean>("triggeredByEvent");

  public static final PropertyKey<Boolean> HAS_CONDITIONAL_EVENTS = new PropertyKey<Boolean>(PROPERTYNAME_HAS_CONDITIONAL_EVENTS);

  public static final PropertyKey<ConditionalEventDefinition> CONDITIONAL_EVENT_DEFINITION = new PropertyKey<ConditionalEventDefinition>("conditionalEventDefinition");

}

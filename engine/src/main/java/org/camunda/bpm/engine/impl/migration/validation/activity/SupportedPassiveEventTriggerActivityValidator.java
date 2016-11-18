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

package org.camunda.bpm.engine.impl.migration.validation.activity;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.behavior.BoundaryEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.behavior.EventSubProcessStartEventActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.ActivityTypes;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

/**
 * Validator for events that passively wait for an event, i.e. without being activated by sequence flow (e.g. boundary events
 * and event subprocess start events but not intermediate catch events).
 *
 * @author Thorben Lindhauer
 */
public class SupportedPassiveEventTriggerActivityValidator implements MigrationActivityValidator {

  public static SupportedPassiveEventTriggerActivityValidator INSTANCE = new SupportedPassiveEventTriggerActivityValidator();

  public static final List<String> supportedTypes = Arrays.asList(
    ActivityTypes.BOUNDARY_MESSAGE,
    ActivityTypes.BOUNDARY_SIGNAL,
    ActivityTypes.BOUNDARY_TIMER,
    ActivityTypes.BOUNDARY_COMPENSATION,
    ActivityTypes.BOUNDARY_CONDITIONAL,
    ActivityTypes.START_EVENT_MESSAGE,
    ActivityTypes.START_EVENT_SIGNAL,
    ActivityTypes.START_EVENT_TIMER,
    ActivityTypes.START_EVENT_COMPENSATION,
    ActivityTypes.START_EVENT_CONDITIONAL
  );

  public boolean valid(ActivityImpl activity) {
    return activity != null && (!isPassivelyWaitingEvent(activity) || isSupportedEventType(activity));
  }

  public boolean isPassivelyWaitingEvent(ActivityImpl activity) {
    return activity.getActivityBehavior() instanceof BoundaryEventActivityBehavior
        || activity.getActivityBehavior() instanceof EventSubProcessStartEventActivityBehavior;
  }

  public boolean isSupportedEventType(ActivityImpl activity) {
    return supportedTypes.contains(activity.getProperties().get(BpmnProperties.TYPE));
  }

}

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
import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

public class SupportedBoundaryEventActivityValidator implements MigrationActivityValidator {

  public static SupportedBoundaryEventActivityValidator INSTANCE = new SupportedBoundaryEventActivityValidator();

  public static final List<String> supportedTypes = Arrays.asList(
    "boundaryMessage",
    "boundarySignal",
    "boundaryTimer"
  );

  public boolean valid(ActivityImpl activity) {
    return activity != null && (!isBoundaryEvent(activity) || isSupportedBoundaryEventType(activity));
  }

  public boolean isBoundaryEvent(ActivityImpl activity) {
    return activity.getActivityBehavior() instanceof BoundaryEventActivityBehavior;
  }

  public boolean isSupportedBoundaryEventType(ActivityImpl activity) {
    return supportedTypes.contains(activity.getProperties().get(BpmnProperties.TYPE));
  }

}

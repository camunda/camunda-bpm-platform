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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.bpmn.behavior.*;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityBehavior;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

/**
 * *Supported* refers to whether an activity instance of a certain activity type can be migrated.
 * This validator is irrelevant for transition instances which can be migrated at any activity type.
 * Thus, this validator is only used during migration instruction generation and migrating activity instance validation,
 * not during migration instruction validation.
 */
public class SupportedActivityValidator implements MigrationActivityValidator {

  public static SupportedActivityValidator INSTANCE = new SupportedActivityValidator();

  public static List<Class<? extends ActivityBehavior>> SUPPORTED_ACTIVITY_BEHAVIORS = new ArrayList<Class<? extends ActivityBehavior>>();

  static {
    SUPPORTED_ACTIVITY_BEHAVIORS.add(SubProcessActivityBehavior.class);
    SUPPORTED_ACTIVITY_BEHAVIORS.add(UserTaskActivityBehavior.class);
    SUPPORTED_ACTIVITY_BEHAVIORS.add(BoundaryEventActivityBehavior.class);
    SUPPORTED_ACTIVITY_BEHAVIORS.add(ParallelMultiInstanceActivityBehavior.class);
    SUPPORTED_ACTIVITY_BEHAVIORS.add(SequentialMultiInstanceActivityBehavior.class);
    SUPPORTED_ACTIVITY_BEHAVIORS.add(ReceiveTaskActivityBehavior.class);
    SUPPORTED_ACTIVITY_BEHAVIORS.add(CallActivityBehavior.class);
    SUPPORTED_ACTIVITY_BEHAVIORS.add(CaseCallActivityBehavior.class);
    SUPPORTED_ACTIVITY_BEHAVIORS.add(IntermediateCatchEventActivityBehavior.class);
    SUPPORTED_ACTIVITY_BEHAVIORS.add(EventBasedGatewayActivityBehavior.class);
    SUPPORTED_ACTIVITY_BEHAVIORS.add(EventSubProcessActivityBehavior.class);
    SUPPORTED_ACTIVITY_BEHAVIORS.add(EventSubProcessStartEventActivityBehavior.class);
    SUPPORTED_ACTIVITY_BEHAVIORS.add(ExternalTaskActivityBehavior.class);
    SUPPORTED_ACTIVITY_BEHAVIORS.add(ParallelGatewayActivityBehavior.class);
    SUPPORTED_ACTIVITY_BEHAVIORS.add(InclusiveGatewayActivityBehavior.class);;
    SUPPORTED_ACTIVITY_BEHAVIORS.add(IntermediateConditionalEventBehavior.class);
    SUPPORTED_ACTIVITY_BEHAVIORS.add(BoundaryConditionalEventActivityBehavior.class);
    SUPPORTED_ACTIVITY_BEHAVIORS.add(EventSubProcessStartConditionalEventActivityBehavior.class);
  }

  public boolean valid(ActivityImpl activity) {
    return activity != null && (isSupportedActivity(activity) || isAsync(activity));
  }

  public boolean isSupportedActivity(ActivityImpl activity) {
    return SUPPORTED_ACTIVITY_BEHAVIORS.contains(activity.getActivityBehavior().getClass());
  }

  protected boolean isAsync(ActivityImpl activity) {
    return activity.isAsyncBefore() || activity.isAsyncAfter();
  }

}

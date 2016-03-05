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

package org.camunda.bpm.engine.impl.migration;

import org.camunda.bpm.engine.impl.bpmn.helper.BpmnProperties;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;

public class DefaultMigrationActivityMatcher implements MigrationActivityMatcher {

  public boolean matchActivities(ActivityImpl source, ActivityImpl target) {
    return source != null && target != null &&
      equalId(source, target) && equalType(source, target);
  }

  protected boolean equalId(ActivityImpl source, ActivityImpl target) {
    return source.getId().equals(target.getId());
  }

  protected boolean equalType(ActivityImpl source, ActivityImpl target) {
    String sourceType = source.getProperties().get(BpmnProperties.TYPE);
    String targetType = target.getProperties().get(BpmnProperties.TYPE);

    return sourceType.equals(targetType);
  }


}

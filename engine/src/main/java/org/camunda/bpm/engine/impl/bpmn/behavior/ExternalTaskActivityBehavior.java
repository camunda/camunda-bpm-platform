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
package org.camunda.bpm.engine.impl.bpmn.behavior;

import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExternalTaskEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

/**
 * Implements behavior of external task activities, i.e. all service-task-like
 * activities that have camunda:type="external".
 *
 * @author Thorben Lindhauer
 */
public class ExternalTaskActivityBehavior extends AbstractBpmnActivityBehavior {

  protected String topicName;

  public ExternalTaskActivityBehavior(String topicName) {
    this.topicName = topicName;
  }

  public void execute(ActivityExecution execution) throws Exception {

    ExternalTaskEntity.createAndInsert((ExecutionEntity) execution, topicName);
  }

  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    leave(execution);
  }
}

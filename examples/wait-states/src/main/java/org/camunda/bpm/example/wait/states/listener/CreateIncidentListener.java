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
package org.camunda.bpm.example.wait.states.listener;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;

/**
 * <p>This is a listener implementation illustrating how to create
 * a custom incident for the current execution.</p>
 */
public class CreateIncidentListener implements ExecutionListener  {

  public void notify(DelegateExecution execution) throws Exception {
    IncidentEntity newIncident = IncidentEntity.createAndInsertIncident("aCustomIncidentType", execution.getId(), execution.getId(), "A custom incident message.");
    newIncident.createRecursiveIncidents();
  }

}

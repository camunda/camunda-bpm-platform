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
package org.camunda.bpm.engine.impl.migration;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.runtime.ActivityInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationLogger extends ProcessEngineLogger {

  public ProcessEngineException unmappedActivityInstances(String processInstanceId, Set<ActivityInstance> unmappedInstances) {
    return new ProcessEngineException(exceptionMessage(
        "001",
        "Process instance " + processInstanceId + " cannot be migrated. There are no migration instructions that apply"
            + " to the following activity instances " + formatActivityInstances(unmappedInstances)));
  }

  protected String formatActivityInstances(Collection<ActivityInstance> activityInstances) {
    StringBuilder sb = new StringBuilder();

    Iterator<ActivityInstance> iterator = activityInstances.iterator();
    while (iterator.hasNext()) {
      sb.append(iterator.next().getId());
      if (iterator.hasNext()) {
        sb.append(", ");
      }
    }

    return sb.toString();
  }

}

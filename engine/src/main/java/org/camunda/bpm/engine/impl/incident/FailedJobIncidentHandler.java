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
package org.camunda.bpm.engine.impl.incident;

/**
 * A incident handler that logs incidents of type <code>failedJob</code>
 * as instances of {@link Incident} to the engine database.
 *
 * <p>
 *
 * This incident handler is active by default and must be disabled
 * via {@link org.camunda.bpm.engine.ProcessEngineConfiguration#setCreateIncidentOnFailedJobEnabled(boolean)}.
 *
 * @see IncidentHandler
 *
 * @author nico.rehwaldt
 * @author roman.smirnov
 * @author Falko Menge
 */
public class FailedJobIncidentHandler extends AbstractIncidentHandler {

  public final static String INCIDENT_HANDLER_TYPE = "failedJob";

  public FailedJobIncidentHandler() {
    super(INCIDENT_HANDLER_TYPE);
  }
}

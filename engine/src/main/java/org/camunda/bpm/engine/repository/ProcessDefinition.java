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
package org.camunda.bpm.engine.repository;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.runtime.ProcessInstance;

/** An object structure representing an executable process composed of
 * activities and transitions.
 *
 * Business processes are often created with graphical editors that store the
 * process definition in certain file format. These files can be added to a
 * {@link Deployment} artifact, such as for example a Business Archive (.bar)
 * file.
 *
 * At deploy time, the engine will then parse the process definition files to an
 * executable instance of this class, that can be used to start a {@link ProcessInstance}.
 *
 * @author Tom Baeyens
 * @author Joram Barez
 * @author Daniel Meyer
 */
public interface ProcessDefinition extends ResourceDefinition {

  /** description of this process **/
  String getDescription();

  /** Does this process definition has a {@link FormService#getStartFormData(String) start form key}. */
  boolean hasStartFormKey();

  /** Returns true if the process definition is in suspended state. */
  boolean isSuspended();

  /** Version tag of the process definition. */
  String getVersionTag();

}

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
package org.camunda.bpm.application.impl;

import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;

/**
 * <p>The process engine holds a strong reference to the embedded process application.</p>
 *
 * @author Daniel Meyer
 *
 */
public class EmbeddedProcessApplicationReferenceImpl implements ProcessApplicationReference {

  protected EmbeddedProcessApplication application;

  public EmbeddedProcessApplicationReferenceImpl(EmbeddedProcessApplication application) {
    this.application = application;
  }

  public String getName() {
    return application.getName();
  }

  public ProcessApplicationInterface getProcessApplication() throws ProcessApplicationUnavailableException {
    return application;
  }

}

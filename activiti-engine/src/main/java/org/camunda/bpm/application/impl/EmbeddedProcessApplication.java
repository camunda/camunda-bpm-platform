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

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationReference;

/**
 * <p>An embedded process application is a ProcessApplication that uses an embedded
 * process engine. An embedded process engine is loaded by the same classloader as 
 * the process application which usually means that the <code>camunda-engine.jar</code> 
 * is deployed as a web application library (in case of WAR deployments) or as an 
 * application library (in case of EAR deployments).</p>
 * 
 * @author Daniel Meyer
 * 
 */
public class EmbeddedProcessApplication extends AbstractProcessApplication {

  protected String autodetectProcessApplicationName() {
    return "Process Application";
  }

  public ProcessApplicationReference getReference() {
    return new ProcessApplicationReferenceImpl(this);
  }

}

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

import java.util.concurrent.Callable;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationExecutionException;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

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

  public static final String DEFAULT_NAME = "Process Application";
  private static ProcessApplicationLogger LOG = ProcessEngineLogger.PROCESS_APPLICATION_LOGGER;

  protected String autodetectProcessApplicationName() {
    return DEFAULT_NAME;
  }

  public ProcessApplicationReference getReference() {
    return new EmbeddedProcessApplicationReferenceImpl(this);
  }

  /**
   * Since the process engine is loaded by the same classloader
   * as the process application, nothing needs to be done.
   */
  public <T> T execute(Callable<T> callable) throws ProcessApplicationExecutionException {
    try {
      return callable.call();
    }
    catch (Exception e) {
      throw LOG.processApplicationExecutionException(e);
    }
  }

}

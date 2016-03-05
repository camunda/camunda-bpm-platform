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
package org.camunda.bpm.integrationtest.functional.context;

import java.util.concurrent.Callable;

import org.camunda.bpm.application.InvocationContext;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.ProcessApplicationExecutionException;
import org.camunda.bpm.application.impl.ServletProcessApplication;

@ProcessApplication("app")
public class ProcessApplicationWithInvocationContext extends ServletProcessApplication {

  private static InvocationContext invocationContext = null;

  @Override
  public <T> T execute(Callable<T> callable, InvocationContext invocationContext) throws ProcessApplicationExecutionException {
    ProcessApplicationWithInvocationContext.invocationContext = invocationContext;

    return execute(callable);
  }

  public static InvocationContext getInvocationContext() {
    return ProcessApplicationWithInvocationContext.invocationContext;
  }

  public static void clearInvocationContext() {
    ProcessApplicationWithInvocationContext.invocationContext = null;
  }

  @Override
  public void undeploy() {
    clearInvocationContext();
    super.undeploy();
  }

}

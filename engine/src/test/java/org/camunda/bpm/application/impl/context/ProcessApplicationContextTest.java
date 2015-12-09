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
package org.camunda.bpm.application.impl.context;

import java.util.concurrent.Callable;

import org.camunda.bpm.application.ProcessApplicationContext;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;
import org.camunda.bpm.application.impl.embedded.TestApplicationWithoutEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.junit.Assert;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessApplicationContextTest extends PluggableProcessEngineTestCase {

  protected TestApplicationWithoutEngine pa;

  public void setUp() {
    pa = new TestApplicationWithoutEngine();
    pa.deploy();
  }

  public void tearDown() {
    pa.undeploy();
  }

  public void testSetPAContextByName() throws ProcessApplicationUnavailableException {

    Assert.assertNull(Context.getCurrentProcessApplication());

    try {
      ProcessApplicationContext.setCurrentProcessApplication(pa.getName());

      Assert.assertEquals(getCurrentContextApplication().getProcessApplication(), pa);
    } finally {
      ProcessApplicationContext.clear();
    }

    Assert.assertNull(Context.getCurrentProcessApplication());
  }

  public void testExecutionInPAContextByName() throws Exception {
    Assert.assertNull(Context.getCurrentProcessApplication());

    ProcessApplicationReference contextPA = ProcessApplicationContext.executeInProcessApplication(
        new Callable<ProcessApplicationReference>() {

          public ProcessApplicationReference call() throws Exception {
            return getCurrentContextApplication();
          }
        },
        pa.getName());

    Assert.assertEquals(contextPA.getProcessApplication(), pa);

    Assert.assertNull(Context.getCurrentProcessApplication());
  }

  public void testSetPAContextByReference() throws ProcessApplicationUnavailableException {
    Assert.assertNull(Context.getCurrentProcessApplication());

    try {
      ProcessApplicationContext.setCurrentProcessApplication(pa.getReference());

      Assert.assertEquals(getCurrentContextApplication().getProcessApplication(), pa);
    } finally {
      ProcessApplicationContext.clear();
    }

    Assert.assertNull(Context.getCurrentProcessApplication());
  }

  public void testExecutionInPAContextByReference() throws Exception {
    Assert.assertNull(Context.getCurrentProcessApplication());

    ProcessApplicationReference contextPA = ProcessApplicationContext.executeInProcessApplication(
        new Callable<ProcessApplicationReference>() {

          public ProcessApplicationReference call() throws Exception {
            return getCurrentContextApplication();
          }
        },
        pa.getReference());

    Assert.assertEquals(contextPA.getProcessApplication(), pa);

    Assert.assertNull(Context.getCurrentProcessApplication());
  }

  public void testSetPAContextByRawPA() throws ProcessApplicationUnavailableException {
    Assert.assertNull(Context.getCurrentProcessApplication());

    try {
      ProcessApplicationContext.setCurrentProcessApplication(pa);

      Assert.assertEquals(getCurrentContextApplication().getProcessApplication(), pa);
    } finally {
      ProcessApplicationContext.clear();
    }

    Assert.assertNull(Context.getCurrentProcessApplication());
  }

  public void testExecutionInPAContextbyRawPA() throws Exception {
    Assert.assertNull(Context.getCurrentProcessApplication());

    ProcessApplicationReference contextPA = ProcessApplicationContext.executeInProcessApplication(
        new Callable<ProcessApplicationReference>() {

          public ProcessApplicationReference call() throws Exception {
            return getCurrentContextApplication();
          }
        },
        pa);

    Assert.assertEquals(contextPA.getProcessApplication(), pa);

    Assert.assertNull(Context.getCurrentProcessApplication());
  }

  protected ProcessApplicationReference getCurrentContextApplication() {
    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    return engineConfiguration.getCommandExecutorTxRequired().execute(new Command<ProcessApplicationReference>() {

      public ProcessApplicationReference execute(CommandContext commandContext) {
        return Context.getCurrentProcessApplication();
      }
    });
  }
}

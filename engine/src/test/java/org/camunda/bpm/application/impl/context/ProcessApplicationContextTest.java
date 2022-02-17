/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.application.impl.context;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;

import org.camunda.bpm.application.InvocationContext;
import org.camunda.bpm.application.ProcessApplicationContext;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationUnavailableException;
import org.camunda.bpm.application.impl.embedded.TestApplicationWithoutEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class ProcessApplicationContextTest extends PluggableProcessEngineTest {

  protected TestApplicationWithoutEngine pa;

  @Before
  public void setUp() {
    pa = new TestApplicationWithoutEngine();
    pa.deploy();
  }

  @After
  public void tearDown() {
    pa.undeploy();
  }

  @Test
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

  @Test
  public void testExecutionInPAContextByName() throws Exception {
    Assert.assertNull(Context.getCurrentProcessApplication());

    ProcessApplicationReference contextPA = ProcessApplicationContext.withProcessApplicationContext(
        new Callable<ProcessApplicationReference>() {

          @Override
          public ProcessApplicationReference call() throws Exception {
            return getCurrentContextApplication();
          }
        },
        pa.getName());

    Assert.assertEquals(contextPA.getProcessApplication(), pa);

    Assert.assertNull(Context.getCurrentProcessApplication());
  }

  @Test
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

  @Test
  public void testExecutionInPAContextByReference() throws Exception {
    Assert.assertNull(Context.getCurrentProcessApplication());

    ProcessApplicationReference contextPA = ProcessApplicationContext.withProcessApplicationContext(
        new Callable<ProcessApplicationReference>() {

          @Override
          public ProcessApplicationReference call() throws Exception {
            return getCurrentContextApplication();
          }
        },
        pa.getReference());

    Assert.assertEquals(contextPA.getProcessApplication(), pa);

    Assert.assertNull(Context.getCurrentProcessApplication());
  }

  @Test
  public void testSetPAContextByRawPA() throws ProcessApplicationUnavailableException {
    Assert.assertNull(Context.getCurrentProcessApplication());

    try {
      ProcessApplicationContext.setCurrentProcessApplication(pa);

      Assert.assertEquals(pa, getCurrentContextApplication().getProcessApplication());
    } finally {
      ProcessApplicationContext.clear();
    }

    Assert.assertNull(Context.getCurrentProcessApplication());
  }

  @Test
  public void testExecutionInPAContextbyRawPA() throws Exception {
    Assert.assertNull(Context.getCurrentProcessApplication());

    ProcessApplicationReference contextPA = ProcessApplicationContext.withProcessApplicationContext(
        new Callable<ProcessApplicationReference>() {

          @Override
          public ProcessApplicationReference call() throws Exception {
            return getCurrentContextApplication();
          }
        },
        pa);

    Assert.assertEquals(contextPA.getProcessApplication(), pa);

    Assert.assertNull(Context.getCurrentProcessApplication());
  }

  @Test
  public void testCannotSetUnregisteredProcessApplicationName() {

    String nonExistingName = pa.getName() + pa.getName();

    try {
      ProcessApplicationContext.setCurrentProcessApplication(nonExistingName);

      try {
        getCurrentContextApplication();
        fail("should not succeed");

      } catch (ProcessEngineException e) {
        testRule.assertTextPresent("A process application with name '" + nonExistingName + "' is not registered", e.getMessage());
      }

    } finally {
      ProcessApplicationContext.clear();
    }
  }

  @Test
  public void testCannotExecuteInUnregisteredPaContext() throws Exception {
    String nonExistingName = pa.getName() + pa.getName();

    try {
      ProcessApplicationContext.withProcessApplicationContext(new Callable<Void>() {

        @Override
        public Void call() throws Exception {
          getCurrentContextApplication();
          return null;
        }

      }, nonExistingName);
      fail("should not succeed");

    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("A process application with name '" + nonExistingName + "' is not registered", e.getMessage());
    }

  }

  @SuppressWarnings("unchecked")
  @Test
  public void testExecuteWithInvocationContext() throws Exception {
    // given a process application which extends the default one
    // - using a spy for verify the invocations
    TestApplicationWithoutEngine processApplication = spy(pa);
    ProcessApplicationReference processApplicationReference = mock(ProcessApplicationReference.class);
    when(processApplicationReference.getProcessApplication()).thenReturn(processApplication);

    // when execute with context
    InvocationContext invocationContext = new InvocationContext(mock(BaseDelegateExecution.class));
    Context.executeWithinProcessApplication(mock(Callable.class), processApplicationReference, invocationContext);

    // then the execute method should be invoked with context
    verify(processApplication).execute(any(Callable.class), eq(invocationContext));
    // and forward to call to the default execute method
    verify(processApplication).execute(any(Callable.class));
  }

  protected ProcessApplicationReference getCurrentContextApplication() {
    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    return engineConfiguration.getCommandExecutorTxRequired().execute(new Command<ProcessApplicationReference>() {

      @Override
      public ProcessApplicationReference execute(CommandContext commandContext) {
        return Context.getCurrentProcessApplication();
      }
    });
  }

}

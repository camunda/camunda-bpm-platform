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
package org.camunda.bpm.spring.boot.starter.event;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JobExecutorStartingEventListenerTest {

  @Mock
  private JobExecutor jobExecutor;

  @InjectMocks
  private JobExecutorStartingEventListener jobExecutorStartingEventListener;

  @Test
  public void handleProcessApplicationStartedEventTest() {
    JobExecutorStartingEventListener spy = Mockito.spy(jobExecutorStartingEventListener);
    spy.handleProcessApplicationStartedEvent(null);
    verify(spy).activate();
  }

  @Test
  public void activateIfNotStartedTest() {
    when(jobExecutor.isActive()).thenReturn(false);
    jobExecutorStartingEventListener.activate();
    verify(jobExecutor).start();
  }

  @Test
  public void doNotActivateIfAlreadyStartedTest() {
    when(jobExecutor.isActive()).thenReturn(true);
    jobExecutorStartingEventListener.activate();
    verify(jobExecutor, times(0)).start();
  }
}

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
package org.camunda.bpm.engine.test.cmmn.tasklistener.util;

import java.io.Serializable;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.TaskListener;

/**
 * @author Roman Smirnov
 *
 */
public class FieldInjectionTaskListener implements TaskListener, Serializable {

  private static final long serialVersionUID = 1L;

  protected Expression greeter;
  protected Expression helloWorld;
  protected Expression prefix;
  protected Expression suffix;

  public void notify(DelegateTask delegateTask) {
    delegateTask.setVariable("greeting", "Hello from " + greeter.getValue(delegateTask));
    delegateTask.setVariable("helloWorld", helloWorld.getValue(delegateTask));
    delegateTask.setVariable("prefix", prefix.getValue(delegateTask));
    delegateTask.setVariable("suffix", suffix.getValue(delegateTask));

    // kind of workaround to pass through the test
    greeter = null;
    helloWorld = null;
    prefix = null;
    suffix = null;
  }

}

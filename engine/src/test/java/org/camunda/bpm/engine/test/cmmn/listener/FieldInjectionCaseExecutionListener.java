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
package org.camunda.bpm.engine.test.cmmn.listener;

import java.io.Serializable;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;
import org.camunda.bpm.engine.delegate.Expression;

/**
 * @author Roman Smirnov
 *
 */
public class FieldInjectionCaseExecutionListener implements CaseExecutionListener, Serializable {

  private static final long serialVersionUID = 1L;

  protected Expression greeter;
  protected Expression helloWorld;
  protected Expression prefix;
  protected Expression suffix;

  public void notify(DelegateCaseExecution caseExecution) {
    caseExecution.setVariable("greeting", "Hello from " + greeter.getValue(caseExecution));
    caseExecution.setVariable("helloWorld", helloWorld.getValue(caseExecution));
    caseExecution.setVariable("prefix", prefix.getValue(caseExecution));
    caseExecution.setVariable("suffix", suffix.getValue(caseExecution));

    // kind of workaround to pass through the test
    greeter = null;
    helloWorld = null;
    prefix = null;
    suffix = null;
  }

}

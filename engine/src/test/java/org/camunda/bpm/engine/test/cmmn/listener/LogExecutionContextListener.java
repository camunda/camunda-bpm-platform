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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.CaseVariableListener;
import org.camunda.bpm.engine.delegate.DelegateCaseVariableInstance;
import org.camunda.bpm.engine.impl.context.CaseExecutionContext;
import org.camunda.bpm.engine.impl.context.Context;

/**
 * @author Thorben Lindhauer
 *
 */
public class LogExecutionContextListener implements CaseVariableListener {

  protected static List<CaseExecutionContext> executionContexts = new ArrayList<CaseExecutionContext>();


  public void notify(DelegateCaseVariableInstance variableInstance) throws Exception {
    executionContexts.add(Context.getCaseExecutionContext());
  }

  public static List<CaseExecutionContext> getCaseExecutionContexts() {
    return executionContexts;
  }

  public static void reset() {
    executionContexts = new ArrayList<CaseExecutionContext>();
  }

}

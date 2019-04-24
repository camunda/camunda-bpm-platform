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
package org.camunda.bpm.engine.test.api.multitenancy.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.delegate.DelegateCaseExecution;

public class AssertingCaseExecutionListener implements CaseExecutionListener {

  public static List<DelegateCaseExecutionAsserter> asserts = new ArrayList<DelegateCaseExecutionAsserter>();

  @Override
  public void notify(DelegateCaseExecution caseExecution) throws Exception {
    for (DelegateCaseExecutionAsserter asserter : asserts) {
      asserter.doAssert(caseExecution);
    }
  }

  public static interface DelegateCaseExecutionAsserter {
    public void doAssert(DelegateCaseExecution caseExecution);
  }

  public static void clear() {
    asserts.clear();
  }

  public static void addAsserts(DelegateCaseExecutionAsserter... asserters) {
    asserts.addAll(Arrays.asList(asserters));
  }

}

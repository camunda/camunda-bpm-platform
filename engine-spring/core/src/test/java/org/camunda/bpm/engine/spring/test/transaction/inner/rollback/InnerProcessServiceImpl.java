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
package org.camunda.bpm.engine.spring.test.transaction.inner.rollback;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.context.ProcessEngineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InnerProcessServiceImpl implements InnerProcessService {

  @Override
  @Transactional(value = "transactionManager", propagation = Propagation.REQUIRES_NEW, rollbackFor = {Throwable.class})
  public void startInnerProcess(DelegateExecution execution) {

    try {
      ProcessEngineContext.requiresNew();
      execution.getProcessEngineServices().getRuntimeService()
        .startProcessInstanceByKey("InnerTxNestedTransactionTest");
    } finally {
      ProcessEngineContext.clear();
    }

    throw new RuntimeException("Inner Transaction Fails and Rolls back error!");
  }
}

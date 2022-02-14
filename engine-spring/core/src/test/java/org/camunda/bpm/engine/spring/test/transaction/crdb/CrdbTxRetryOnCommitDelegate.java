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
package org.camunda.bpm.engine.spring.test.transaction.crdb;

import java.sql.SQLException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;

@Service("simulateCommitFailureDelegate")
public class CrdbTxRetryOnCommitDelegate implements JavaDelegate {

  protected static boolean throwException = true;

  @Override
  public void execute(DelegateExecution execution) throws Exception {

      if (throwException) {
          throwException = false;
          // simulate a CRDB TX Error on TX Commit
          throw new TransactionSystemException("CRDB Error:",
              new SQLException("ERROR: restart transaction: TransactionRetryWithProtoRefreshError: " +
                  "TransactionRetryError: retry txn (RETRY_SERIALIZABLE)"));
      }
  }

}
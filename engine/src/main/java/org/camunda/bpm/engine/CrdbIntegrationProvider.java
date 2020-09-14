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
package org.camunda.bpm.engine;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CrdbTransactionRetryInterceptor;

public interface CrdbIntegrationProvider {

  /**
   * This method determines if a {@link CrdbTransactionRetryInterceptor} needs to be registered
   * for the current Process Engine. By default, it will only be true when CockroachDB is used.
   *
   * @param configuration - the {@link ProcessEngineConfiguration} instance where
   *                      the {@link CrdbTransactionRetryInterceptor} needs to be registered.
   * @return true, if the interceptor should be registered. Otherwise, it is false.
   */
  default boolean registerCrdbRetryInterceptor(ProcessEngineConfiguration configuration) {
    return DbSqlSessionFactory.CRDB.equals(configuration.getDatabaseType());
  };

  /**
   *
   * @param commandRetries - the number of Command retries the {@link CrdbTransactionRetryInterceptor}
   *                       is allowed to perform. This parameter is provided by the {@link ProcessEngineConfigurationImpl}
   *                       instance, when this method is invoked.
   * @return an object that is an implementation of the {@link CrdbTransactionRetryInterceptor}.
   */
  default CrdbTransactionRetryInterceptor getCrdbRetryInterceptor(int commandRetries){
    return new CrdbTransactionRetryInterceptor(commandRetries);
  };

}
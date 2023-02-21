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
package org.camunda.bpm.engine.impl.cfg;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import jakarta.transaction.RollbackException;
import jakarta.transaction.TransactionManager;
import java.sql.SQLException;
import org.camunda.bpm.engine.CrdbTransactionRetryException;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.camunda.bpm.engine.impl.interceptor.JakartaTransactionInterceptor;
import org.camunda.bpm.engine.impl.interceptor.TransactionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * This test covers the exception handling of the Jakarta Transaction Interceptor, especially
 * when used with CockroachDB. To avoid a complex setup (CRDB + engine with JTA configuration),
 * it mocks the involved resources. This is not great, but better than no test.
 */
@RunWith(MockitoJUnitRunner.class)
public class JakartaTransactionInterceptorTest {


  @Mock
  public TransactionManager txManager;

  @Mock
  public ProcessEngineConfigurationImpl engineConfiguration;

  @Mock
  public CommandInterceptor nextInterceptor;

  @Before
  public void mockEngineConfiguration() {
    when(engineConfiguration.getDatabaseType()).thenReturn(DbSqlSessionFactory.CRDB);
  }

  @Test
  public void shouldConvertSqlExceptionToCrdbError() throws Exception {

    // given
    JakartaTransactionInterceptor interceptor = new JakartaTransactionInterceptor(
        txManager, true, engineConfiguration);
    interceptor.setNext(nextInterceptor);

    RollbackException exception = new RollbackException();
    exception.addSuppressed(buildCrdbCommitException());

    doThrow(exception).when(txManager).commit();

    // when/then
    assertThatThrownBy(() -> interceptor.execute(c -> null))
      .isInstanceOf(CrdbTransactionRetryException.class);
  }

  @Test
  public void shoulNotConvertUnrelatedSqlExceptionToCrdbError() throws Exception {

    // given
    JakartaTransactionInterceptor interceptor = new JakartaTransactionInterceptor(
        txManager, true, engineConfiguration);
    interceptor.setNext(nextInterceptor);

    RollbackException exception = new RollbackException();
    exception.addSuppressed(new SQLException("unrelated error"));

    doThrow(exception).when(txManager).commit();

    // when/then the exception is converted to a CRDB exception that can be retried
    assertThatThrownBy(() -> interceptor.execute(c -> null))
      .isInstanceOf(TransactionException.class)
      .hasMessage("Unable to commit transaction");
  }


  @Test
  public void shoulNotConvertGenericRuntimeExceptionToCrdbError() throws Exception {

    // given
    JakartaTransactionInterceptor interceptor = new JakartaTransactionInterceptor(
        txManager, true, engineConfiguration);
    interceptor.setNext(nextInterceptor);

    doThrow(new RuntimeException()).when(txManager).commit();

    // when/then the exception is converted to a CRDB exception that can be retried
    assertThatThrownBy(() -> interceptor.execute(c -> null))
      .isInstanceOf(RuntimeException.class);
  }

  private Exception buildCrdbCommitException() {
    return new SQLException("ERROR: restart transaction: TransactionRetryWithProtoRefreshError: " +
        "TransactionRetryError: retry txn (RETRY_SERIALIZABLE)");
  }

}

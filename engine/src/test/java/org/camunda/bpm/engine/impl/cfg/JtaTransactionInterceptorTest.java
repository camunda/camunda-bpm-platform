package org.camunda.bpm.engine.impl.cfg;

import java.sql.SQLException;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import javax.transaction.RollbackException;
import javax.transaction.TransactionManager;

import org.camunda.bpm.engine.CrdbTransactionRetryException;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;
import org.camunda.bpm.engine.impl.interceptor.JtaTransactionInterceptor;
import org.camunda.bpm.engine.impl.interceptor.JtaTransactionInterceptor.TransactionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * This test covers the exception handling of the JTA transaction interceptor, especially
 * when used with CockroachDB. To avoid a complex setup (CRDB + engine with JTA configuration),
 * it mocks the involved resources. This is not great, but better than no test.
 */
@RunWith(MockitoJUnitRunner.class)
public class JtaTransactionInterceptorTest {


  @Mock
  public TransactionManager txManager;

  @Mock
  public ProcessEngineConfigurationImpl engineConfiguration;

  @Mock
  public CommandInterceptor nextInterceptor;

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Before
  public void mockEngineConfiguration() {
    when(engineConfiguration.getDatabaseType()).thenReturn(DbSqlSessionFactory.CRDB);
  }

  @Test
  public void shouldConvertSqlExceptionToCrdbError() throws Exception {

    // given
    JtaTransactionInterceptor interceptor = new JtaTransactionInterceptor(
        txManager, true, engineConfiguration);
    interceptor.setNext(nextInterceptor);

    RollbackException exception = new RollbackException();
    exception.addSuppressed(buildCrdbCommitException());

    doThrow(exception).when(txManager).commit();

    // then the exception is converted to a CRDB exception that can be retried
    exceptionRule.expect(CrdbTransactionRetryException.class);

    // when
    interceptor.execute(c -> null);
  }

  @Test
  public void shoulNotConvertUnrelatedSqlExceptionToCrdbError() throws Exception {

    // given
    JtaTransactionInterceptor interceptor = new JtaTransactionInterceptor(
        txManager, true, engineConfiguration);
    interceptor.setNext(nextInterceptor);

    RollbackException exception = new RollbackException();
    exception.addSuppressed(new SQLException("unrelated error"));

    doThrow(exception).when(txManager).commit();

    // then the exception is converted to a CRDB exception that can be retried
    exceptionRule.expect(TransactionException.class);
    exceptionRule.expectMessage("Unable to commit transaction");

    // when
    interceptor.execute(c -> null);
  }


  @Test
  public void shoulNotConvertGenericRuntimeExceptionToCrdbError() throws Exception {

    // given
    JtaTransactionInterceptor interceptor = new JtaTransactionInterceptor(
        txManager, true, engineConfiguration);
    interceptor.setNext(nextInterceptor);

    doThrow(new RuntimeException()).when(txManager).commit();

    // then the exception is converted to a CRDB exception that can be retried
    exceptionRule.expect(RuntimeException.class);

    // when
    interceptor.execute(c -> null);
  }

  private Exception buildCrdbCommitException() {
    return new SQLException("ERROR: restart transaction: TransactionRetryWithProtoRefreshError: " +
        "TransactionRetryError: retry txn (RETRY_SERIALIZABLE)");
  }

}

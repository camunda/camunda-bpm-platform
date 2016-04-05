package org.camunda.bpm.engine.test.standalone.entity;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseSentryPartEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseSentryPartQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;

/**
 * @author Kristin Polenz
 */
public class CaseSentryPartEntityTest extends PluggableProcessEngineTestCase {

  public void testSentryWithTenantId() {
    CaseSentryPartEntity caseSentryPartEntity = new CaseSentryPartEntity();
    caseSentryPartEntity.setTenantId("tenant1");

    insertCaseSentryPart(caseSentryPartEntity);

    caseSentryPartEntity = readCaseSentryPart();
    assertThat(caseSentryPartEntity.getTenantId(), is("tenant1"));

    deleteCaseSentryPart(caseSentryPartEntity);
  }

  protected void insertCaseSentryPart(final CaseSentryPartEntity caseSentryPartEntity) {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        commandContext.getCaseSentryPartManager().insert(caseSentryPartEntity);
        return null;
      }
    });
  }

  protected CaseSentryPartEntity readCaseSentryPart() {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequiresNew();
    return new CaseSentryPartQueryImpl(commandExecutor).singleResult();
  }

  protected void deleteCaseSentryPart(final CaseSentryPartEntity caseSentryPartEntity) {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {

      @Override
      public Void execute(CommandContext commandContext) {
        commandContext.getCaseSentryPartManager().delete(caseSentryPartEntity);
        return null;
      }
    });
  }

}

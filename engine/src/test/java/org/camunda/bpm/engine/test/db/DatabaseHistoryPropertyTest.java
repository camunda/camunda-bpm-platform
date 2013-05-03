package org.camunda.bpm.engine.test.db;

import java.util.Map;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.ProcessEngineImpl;
import org.camunda.bpm.engine.impl.SchemaOperationsProcessEngineBuild;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Christian Lipphardt
 */
@Ignore
public class DatabaseHistoryPropertyTest {

  private static ProcessEngineImpl processEngineImpl;

  // make sure schema is dropped
  @BeforeClass
  public static void initialize() {
    ProcessEngineImpl processEngine = createProcessEngineImpl("false", false);
    try {
      TestHelper.dropSchema(processEngine.getProcessEngineConfiguration());
    } catch (RuntimeException e) {
      // ignore
    }
    processEngine.close();
    processEngine = null;
  }
  
  // make sure schema is dropped
  @After
  public void cleanup() {
    TestHelper.dropSchema(processEngineImpl.getProcessEngineConfiguration());
    processEngineImpl.close();
    processEngineImpl = null;
  }
  
  // make sure we have a schema available after running this test class
  @AfterClass
  public static void shutdown() {
    ProcessEngineImpl processEngine = createProcessEngineImpl("false", false);
    TestHelper.createSchema(processEngine.getProcessEngineConfiguration());
    processEngine.close();
    processEngine = null;
  }
  
  @Test
  public void schemaCreatedByEngineAndDatabaseSchemaUpdateTrue() {
    processEngineImpl = createProcessEngineImpl("true", true);
    
    assertHistoryLevel();
  }

  @Test
  public void schemaCreatedByUserAndDatabaseSchemaUpdateTrue() {
    processEngineImpl = createProcessEngineImpl("true", false);
    // simulate manual schema creation by user
    TestHelper.createSchema(processEngineImpl.getProcessEngineConfiguration());
    
    // let the engine do their schema operations thing
    processEngineImpl.getProcessEngineConfiguration()
    .getCommandExecutorSchemaOperations()
    .execute(new SchemaOperationsProcessEngineBuild());
    
    assertHistoryLevel();
  }
  
  @Test
  public void schemaCreatedByUserAndDatabaseSchemaUpdateFalse() {
    processEngineImpl = createProcessEngineImpl("false", false);
    // simulate manual schema creation by user
    TestHelper.createSchema(processEngineImpl.getProcessEngineConfiguration());
    
    // let the engine do their schema operations thing
    processEngineImpl.getProcessEngineConfiguration()
    .getCommandExecutorSchemaOperations()
    .execute(new SchemaOperationsProcessEngineBuild());
    
    assertHistoryLevel();
  }
  
  private void assertHistoryLevel() {
    Map<String, String> properties = processEngineImpl.getManagementService().getProperties();
    String historyLevel = properties.get("historyLevel");
    Assert.assertNotNull("historyLevel is null -> not set in database", historyLevel);
    Assert.assertEquals(ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL, Integer.parseInt(historyLevel));
  }
  
  
  //----------------------- TEST HELPERS -----------------------
  
  private static class CreateSchemaProcessEngineImpl extends ProcessEngineImpl {
    public CreateSchemaProcessEngineImpl(ProcessEngineConfigurationImpl processEngineConfiguration) {
      super(processEngineConfiguration);
    }
    
    protected void executeSchemaOperations() {
      super.executeSchemaOperations();
    }
  }
  
  private static class CreateNoSchemaProcessEngineImpl extends ProcessEngineImpl {
    public CreateNoSchemaProcessEngineImpl(ProcessEngineConfigurationImpl processEngineConfiguration) {
      super(processEngineConfiguration);
    }
    
    protected void executeSchemaOperations() {
      // nop - do not execute create schema operations
    }
  }
  
  // allows to return a process engine configuration which doesn't create a schema when it's build.
  private static class CustomStandaloneInMemProcessEngineConfiguration extends StandaloneInMemProcessEngineConfiguration {
    
    boolean executeSchemaOperations;
    
    public ProcessEngine buildProcessEngine() {
      init();
      if (executeSchemaOperations) {
        return new CreateSchemaProcessEngineImpl(this);
      } else {
        return new CreateNoSchemaProcessEngineImpl(this);
      }
    }
    
    public ProcessEngineConfigurationImpl setExecuteSchemaOperations(boolean executeSchemaOperations) {
      this.executeSchemaOperations = executeSchemaOperations;
      return this;
    }
  }
  
  private static ProcessEngineImpl createProcessEngineImpl(String databaseSchemaUpdate, boolean executeSchemaOperations) {
    ProcessEngineImpl processEngine = 
        (ProcessEngineImpl) new CustomStandaloneInMemProcessEngineConfiguration()
               .setExecuteSchemaOperations(executeSchemaOperations)
               .setDatabaseSchemaUpdate(databaseSchemaUpdate)
               .setHistory(ProcessEngineConfiguration.HISTORY_FULL)
               .buildProcessEngine();
    return processEngine;
  }
  
}

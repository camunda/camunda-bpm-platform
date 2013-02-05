package com.camunda.fox.platform.test.functional.transactions;

import junit.framework.Assert;

import org.activiti.engine.impl.cfg.TransactionListener;
import org.activiti.engine.impl.cfg.TransactionState;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;

/**
 * 
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class TransactionListenerTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {
    return initWebArchiveDeployment();
  }
  
  @Test
  public void testSynchronizationOnRollback() {
    
    final TestTransactionListener rolledBackListener = new TestTransactionListener();
    final TestTransactionListener committedListener = new TestTransactionListener();
    Assert.assertFalse(rolledBackListener.isInvoked());
    Assert.assertFalse(committedListener.isInvoked());
    
    try {
      
      processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
        
        public Void execute(CommandContext commandContext) {         
          commandContext.getTransactionContext().addTransactionListener(TransactionState.ROLLED_BACK, rolledBackListener);
          commandContext.getTransactionContext().addTransactionListener(TransactionState.COMMITTED, committedListener);  
          
          throw new RuntimeException("Booum! Rollback!");
        }
        
      });
      
    }catch(Exception e) {
      Assert.assertTrue(e.getMessage().contains("Rollback!"));
    }
    
    Assert.assertTrue(rolledBackListener.isInvoked());
    Assert.assertFalse(committedListener.isInvoked());
    
  }
  
  @Test
  public void testSynchronizationOnCommitted() {
    
    final TestTransactionListener rolledBackListener = new TestTransactionListener();
    final TestTransactionListener committedListener = new TestTransactionListener();
    
    Assert.assertFalse(rolledBackListener.isInvoked());
    Assert.assertFalse(committedListener.isInvoked());
    
    try {
      
      processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Void>() {
        
        public Void execute(CommandContext commandContext) {         
          commandContext.getTransactionContext().addTransactionListener(TransactionState.ROLLED_BACK, rolledBackListener);
          commandContext.getTransactionContext().addTransactionListener(TransactionState.COMMITTED, committedListener);  
          return null;
        }
        
      });
      
    }catch(Exception e) {
      Assert.assertTrue(e.getMessage().contains("Rollback!"));
    }
    
    Assert.assertFalse(rolledBackListener.isInvoked());
    Assert.assertTrue(committedListener.isInvoked());
    
  }
  
  protected static class TestTransactionListener implements TransactionListener {

    protected volatile boolean invoked = false;
    
    public void execute(CommandContext commandContext) {
      invoked = true;
    }
    
    public boolean isInvoked() {
      return invoked;
    }
    
  }

}

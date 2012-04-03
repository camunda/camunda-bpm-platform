package com.camunda.fox.cockpit.demo.deployer;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import com.camunda.fox.platform.spi.ProcessArchive;
import com.camunda.fox.platform.spi.ProcessArchiveCallback;

/**
 * <p>Singleton bean which allows to execute callbacks in the context the 
 * carrier process archive.</p>
 *  
 * @author Daniel Meyer
 * @see ProcessArchive#executeWithinContext(ProcessArchiveCallback)
 */
// singleton bean guarantees maximum efficiency
@Singleton
// make sure the container does not rollback transactions if this bean throws an exception
@TransactionManagement(TransactionManagementType.BEAN)
//make sure the container does not synchronize access to this bean
@ConcurrencyManagement(ConcurrencyManagementType.BEAN) 
public class ProcessArchiveContextExecutor {
  
  public <T> T executeWithinContext(ProcessArchiveCallback<T> callback) throws
  // gets past the EJB container (must be unwrapped by caller)
  FoxApplicationException  
  {
    try {
      return callback.execute();  
    }catch (RuntimeException e) {
      throw new FoxApplicationException("Caught Exception", e);
    }
  }
  
}

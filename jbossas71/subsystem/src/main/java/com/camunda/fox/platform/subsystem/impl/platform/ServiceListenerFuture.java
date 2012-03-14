package com.camunda.fox.platform.subsystem.impl.platform;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceListener;
import org.jboss.msc.service.ServiceController.Substate;
import org.jboss.msc.service.ServiceController.Transition;

/**
 * <p>A future implementation backed by a {@link ServiceListener}</p>
 * 
 * @author Daniel Meyer
 * 
 */
public abstract class ServiceListenerFuture<S, V> extends AbstractServiceListener<S> implements ServiceListener<S>, Future<V> {

  protected final S serviceInstance;
    
  public ServiceListenerFuture(S serviceInstance) {
    this.serviceInstance = serviceInstance;
  }
  
  protected V value;
  boolean cancelled;
  
  @Override
  public void transition(ServiceController< ? extends S> controller, Transition transition) {
    if(transition.getAfter().equals(Substate.UP)) {
      serviceAvailable();
      synchronized(this)  {
        this.notifyAll();
      }
    } else if(transition.getAfter().equals(Substate.CANCELLED)){
      cancelled = true;
      synchronized(this)  {
        this.notifyAll();
      }
    }    
  }
  
  protected abstract void serviceAvailable();

  public boolean cancel(boolean mayInterruptIfRunning) {
    // unsupported
    return false;
  }

  public boolean isCancelled() {
    // unsupported
    return cancelled;
  }

  public boolean isDone() {
    return value != null;
  }

  public V get() throws InterruptedException, ExecutionException {
    if (value == null) {
      synchronized (this) {
        if (value == null) {
          this.wait();          
        }
      }
    }
    return value;
  }

  public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    if (value == null) {
      synchronized (this) {
        if (value == null) {
          this.wait(unit.convert(timeout, TimeUnit.MILLISECONDS));
        }
        synchronized (this) {
          if (cancelled || value == null) {
            throw new TimeoutException();
          }
        }
      }
    }
    return value;
  }

}

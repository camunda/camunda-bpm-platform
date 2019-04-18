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
package org.camunda.bpm.container.impl.jboss.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Substate;
import org.jboss.msc.service.ServiceController.Transition;
import org.jboss.msc.service.ServiceListener;

/**
 * <p>A {@link Future} implementation backed by a {@link ServiceListener}</p>
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
  boolean failed;
  
  @Override
  public void transition(ServiceController< ? extends S> controller, Transition transition) {
    if(transition.getAfter().equals(Substate.UP)) {
      serviceAvailable();
      synchronized(this)  {
        this.notifyAll();
      }
    } else if(transition.getAfter().equals(Substate.CANCELLED)){
      synchronized(this)  {
        cancelled = true;
        this.notifyAll();
      }
    } else if(transition.getAfter().equals(Substate.START_FAILED)) {
      synchronized (this) {
        failed = true;
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
    if (!failed && !cancelled && value == null) {
      synchronized (this) {
        if (!failed && !cancelled && value == null) {
          this.wait();          
        }
      }
    }
    return value;
  }

  public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    if (!failed && !cancelled && value == null) {
      synchronized (this) {
        if (!failed && !cancelled && value == null) {
          this.wait(unit.convert(timeout, TimeUnit.MILLISECONDS));
        }
        synchronized (this) {
          if (value == null) {
            throw new TimeoutException();
          }
        }
      }
    }
    return value;
  }

}

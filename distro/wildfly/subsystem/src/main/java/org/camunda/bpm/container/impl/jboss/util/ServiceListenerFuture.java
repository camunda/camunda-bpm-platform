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

import org.jboss.msc.service.LifecycleEvent;
import org.jboss.msc.service.LifecycleListener;
import org.jboss.msc.service.ServiceController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * <p>A {@link Future} implementation backed by a {@link LifecycleListener}</p>
 *
 * @author Daniel Meyer
 *
 */
public abstract class ServiceListenerFuture<S, V> implements LifecycleListener, Future<V> {

  protected final S serviceInstance;

  public ServiceListenerFuture(S serviceInstance) {
    this.serviceInstance = serviceInstance;
  }

  protected V value;
  boolean cancelled;
  boolean failed;

  @Override
  public void handleEvent(final ServiceController<?> controller, final LifecycleEvent event) {
    if(event.equals(LifecycleEvent.UP)) {
      serviceAvailable();
      synchronized(this)  {
        this.notifyAll();
      }
    } else if(event.equals(LifecycleEvent.FAILED)) {
      synchronized (this) {
        failed = true;
        this.notifyAll();
      }
    } else {
      synchronized(this)  {
        cancelled = true;
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

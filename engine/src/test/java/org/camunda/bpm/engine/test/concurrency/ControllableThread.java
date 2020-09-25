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
package org.camunda.bpm.engine.test.concurrency;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.slf4j.Logger;


/**
 * @author Tom Baeyens
 */
public class ControllableThread extends Thread {

  private static Logger LOG = ProcessEngineLogger.TEST_LOGGER.getLogger();

  public ControllableThread() {
    super();
    setName(generateName());
  }

  public ControllableThread(Runnable runnable) {
    super(runnable);
    setName(generateName());
  }

  protected String generateName() {
    String className = getClass().getName();
    int dollarIndex = className.lastIndexOf('$');
    return className.substring(dollarIndex+1);
  }

  public synchronized void startAndWaitUntilControlIsReturned() {
    LOG.debug("test thread will start "+getName()+" and wait till it returns control");
    start();
    try {
      wait();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public synchronized void returnControlToTestThreadAndWait() {
    LOG.debug(getName()+" will notify test thread and till test thread proceeds this thread");
    this.notify();
    try {
      this.wait();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public synchronized void proceedAndWaitTillDone() {
    LOG.debug("test thread will notify "+getName()+" and wait until it completes");
    notify();
    try {
      join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}

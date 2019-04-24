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
package org.camunda.bpm.engine.impl.db;

import org.camunda.bpm.engine.impl.cfg.IdGenerator;
import org.camunda.bpm.engine.impl.cmd.GetNextIdBlockCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;


/**
 * @author Tom Baeyens
 */
public class DbIdGenerator implements IdGenerator {

  protected int idBlockSize;
  protected long nextId;
  protected long lastId;

  protected CommandExecutor commandExecutor;

  public DbIdGenerator() {
    reset();
  }

  public synchronized String getNextId() {
    if (lastId<nextId) {
      getNewBlock();
    }
    long _nextId = nextId++;
    return Long.toString(_nextId);
  }

  protected synchronized void getNewBlock() {
    // TODO http://jira.codehaus.org/browse/ACT-45 use a separate 'requiresNew' command executor
    IdBlock idBlock = commandExecutor.execute(new GetNextIdBlockCmd(idBlockSize));
    this.nextId = idBlock.getNextId();
    this.lastId = idBlock.getLastId();
  }

  public int getIdBlockSize() {
    return idBlockSize;
  }

  public void setIdBlockSize(int idBlockSize) {
    this.idBlockSize = idBlockSize;
  }

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public void setCommandExecutor(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  /**
   * Reset inner state so that the generator fetches a new block of IDs from the database
   * when the next ID generation request is received.
   */
  public void reset() {
    nextId = 0;
    lastId = -1;
  }
}

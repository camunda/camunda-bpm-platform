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
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import java.io.Serializable;


/**
 * @author Joram Barrez
 */
public class DeleteProcessInstanceCmd extends AbstractDeleteProcessInstanceCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String processInstanceId;
  protected boolean skipIoMappings;
  protected boolean skipSubprocesses;


  public DeleteProcessInstanceCmd(String processInstanceId, String deleteReason, boolean skipCustomListeners, boolean externallyTerminated,
      boolean skipIoMappings, boolean skipSubprocesses, boolean failIfNotExists) {
    this.processInstanceId = processInstanceId;
    this.deleteReason = deleteReason;
    this.skipCustomListeners = skipCustomListeners;
    this.externallyTerminated = externallyTerminated;
    this.skipIoMappings = skipIoMappings;
    this.skipSubprocesses = skipSubprocesses;
    this.failIfNotExists = failIfNotExists;
  }

  public Void execute(CommandContext commandContext) {
    deleteProcessInstance(commandContext, processInstanceId, deleteReason, skipCustomListeners, externallyTerminated, skipIoMappings, skipSubprocesses);

    return null;
  }

}

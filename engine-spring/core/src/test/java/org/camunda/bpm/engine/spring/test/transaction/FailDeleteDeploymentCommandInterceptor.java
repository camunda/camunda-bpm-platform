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
package org.camunda.bpm.engine.spring.test.transaction;

import org.camunda.bpm.engine.impl.cmd.DeleteDeploymentCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;

/**
 * @author Svetlana Dorokhova
 *
 */
public class FailDeleteDeploymentCommandInterceptor extends CommandInterceptor {

  @Override
  public <T> T execute(Command<T> command) {

    T result = next.execute(command);

    if (command instanceof DeleteDeploymentCmd) {
      throw new RuntimeException("roll back transaction");
    }

    return result;
  }
}

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
package org.camunda.bpm.container.impl.jboss.extension.handler;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.dmr.ModelNode;

/**
 * @author Daniel Meyer
 * @author Christian Lipphardt
 */
public class BpmPlatformSubsystemRemove extends ReloadRequiredRemoveStepHandler {

  public static final BpmPlatformSubsystemRemove INSTANCE = new BpmPlatformSubsystemRemove();

  private BpmPlatformSubsystemRemove() {
  }

  protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
    super.performRuntime(context, operation, model);
  }

  protected void recoverServices(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
    super.recoverServices(context, operation, model);

  }
}

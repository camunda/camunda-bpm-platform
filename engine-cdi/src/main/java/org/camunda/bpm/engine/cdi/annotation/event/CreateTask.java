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
package org.camunda.bpm.engine.cdi.annotation.event;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Can be used to qualify events fired when a task is created.
 * 
 * <pre>
 * public void onApproveRegistrationTaskCreate(@Observes @CreateTask("approveRegistration") BusinessProcessEvent evt) {
 *   // ...
 * }
 * </pre>
 * 
 * @author Daniel Meyer
 * @author Sebastian Menski
 */
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface CreateTask {
  /** the definition key (id of the task in BPMN XML) of the task which was created */
  public String value();
}

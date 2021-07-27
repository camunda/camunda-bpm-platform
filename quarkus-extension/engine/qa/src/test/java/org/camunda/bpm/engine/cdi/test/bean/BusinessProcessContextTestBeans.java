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
package org.camunda.bpm.engine.cdi.test.bean;

import org.camunda.bpm.engine.cdi.test.impl.beans.CreditCard;
import org.camunda.bpm.engine.cdi.test.impl.beans.ProcessScopedMessageBean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

// TODO: Remove with CAM-13754
public class BusinessProcessContextTestBeans {

  @ApplicationScoped
  @Named("creditCard")
  public static class QuarkusCreditCard extends CreditCard {
  }

  @ApplicationScoped
  @Named("processScopedMessageBean")
  public static class QuarkusProcessScopedMessageBean extends ProcessScopedMessageBean {
  }

}

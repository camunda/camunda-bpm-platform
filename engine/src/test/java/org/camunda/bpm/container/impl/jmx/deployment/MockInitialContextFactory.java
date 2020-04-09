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
package org.camunda.bpm.container.impl.jmx.deployment;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import java.util.Hashtable;

public class MockInitialContextFactory implements InitialContextFactory {

  private static final ThreadLocal<Context> currentContext = new ThreadLocal<Context>();

  @Override
  public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
    return currentContext.get();
  }

  public static void setCurrentContext(Context context) {
    currentContext.set(context);
  }

  public static void clearCurrentContext() {
    currentContext.remove();
  }

}

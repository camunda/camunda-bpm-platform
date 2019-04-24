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
package org.camunda.bpm.engine.impl.identity.db;

import java.util.List;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.impl.GroupQueryImpl;
import org.camunda.bpm.engine.impl.Page;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 * @author Daniel Meyer
 *
 */
public class DbGroupQueryImpl extends GroupQueryImpl {

  private static final long serialVersionUID = 1L;

  public DbGroupQueryImpl() {
    super();
  }

  public DbGroupQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  //results ////////////////////////////////////////////////////////

  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    DbReadOnlyIdentityServiceProvider identityProvider = getIdentityProvider(commandContext);
    return identityProvider.findGroupCountByQueryCriteria(this);
  }

  public List<Group> executeList(CommandContext commandContext, Page page) {
    checkQueryOk();
    DbReadOnlyIdentityServiceProvider identityProvider = getIdentityProvider(commandContext);
    return identityProvider.findGroupByQueryCriteria(this);
  }

  protected DbReadOnlyIdentityServiceProvider getIdentityProvider(CommandContext commandContext) {
    return (DbReadOnlyIdentityServiceProvider) commandContext.getReadOnlyIdentityProvider();
  }

}

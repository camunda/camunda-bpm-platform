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
package org.camunda.bpm.engine.test.api.identity.plugin;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.identity.db.DbIdentityServiceProvider;

/**
 * To create a testcase, that tests the write a Member/Group/Membership in
 *  on step, an entry Point into the write option within the same Command Context is needed.
 *  This is done by extending the to-test class and overriding a not in scope Method.
 *  This Method will trigger the write of Member/Group/Membership in one step.
 *  <br><br>
 *  The Group will be the userId extended by _group
 *  <br><br>
 *  The checkPassword method must return true, because exactly the requested user with the
 *  requested Password will be created within this Method
 *
 *  @author Simon Jonischkeit
 */
public class TestDbIdentityServiceProviderExtension extends DbIdentityServiceProvider{

  @Override
  public boolean checkPassword(final String userId, final String password) {

    // Create and Save a User
    final User user = super.createNewUser(userId);
    user.setPassword(password);
    super.saveUser(user);

    // Create and Save a Group
    final String groupId = userId+"_group";
    final Group group = super.createNewGroup(groupId);
    group.setName(groupId);
    super.saveGroup(group);

    // Create the corresponding Membership
    super.createMembership(userId, groupId);

    return super.checkPassword(userId, password);
  }
}

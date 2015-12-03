/**
 *
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

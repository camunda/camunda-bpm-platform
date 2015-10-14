/**
 *
 */
package org.camunda.bpm.engine.test.db.identity;

import java.util.List;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.junit.Assert;

/**
 * @author Simon Jonischkeit
 *
 */
public class WriteMultipleEntitiesInOneStepTest extends ResourceProcessEngineTestCase {
  /**
   *
   */
  public WriteMultipleEntitiesInOneStepTest() {
    super("org/camunda/bpm/engine/test/db/WriteMultipleEntitiesInOneStepTest.camunda.cfg.xml");
  }

  public void testWriteMultipleEntitysInOneStep(){

    // The User will be saved into the Db through the plugin in the plugin package beside the Test.
    // The GroupCreated is the userId_group
    Assert.assertTrue(identityService.checkPassword("multipleEntities", "inOneStep"));
    User user = identityService.createUserQuery().userId("multipleEntities").singleResult();

    Assert.assertNotNull(user);
    Assert.assertEquals("multipleEntities", user.getId());
    Assert.assertEquals("{SHA}pfdzmt+49nwknTy7xhZd7ZW5suI=", user.getPassword());

    // It is expected, that the User is in exactly one Group
    List<Group> groups = this.identityService.createGroupQuery().groupMember("multipleEntities").list();
    Assert.assertEquals(1, groups.size());

    Group group = groups.get(0);
    Assert.assertEquals("multipleEntities_group", group.getId());

    // clean the Db
    identityService.deleteMembership("multipleEntities", "multipleEntities_group");
    identityService.deleteGroup("multipleEntities_group");
    identityService.deleteUser("multipleEntities");
  }
}

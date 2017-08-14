package org.camunda.bpm.spring.boot.starter.property;

import org.camunda.bpm.engine.identity.User;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminUserPropertyTest {

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void fillMissingFields_fail_no_id() throws Exception {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("missing field: camunda.bpm.admin-user.id");

    adminUser(null, null, null, null, null).init();
  }

  @Test
  public void fillMissingFields_null() throws Exception {
    User adminUser = adminUser("admin", "foo", null, null, null).init();

    assertThat(adminUser.getId()).isEqualTo("admin");
    assertThat(adminUser.getPassword()).isEqualTo("foo");
    assertThat(adminUser.getFirstName()).isEqualTo("Admin");
    assertThat(adminUser.getLastName()).isEqualTo("Admin");
    assertThat(adminUser.getEmail()).isEqualTo("admin@localhost");
  }

  @Test
  public void fillMissingFields_blank() throws Exception {
    User adminUser = adminUser("admin", "foo", "", "", "").init();

    assertThat(adminUser.getId()).isEqualTo("admin");
    assertThat(adminUser.getPassword()).isEqualTo("foo");
    assertThat(adminUser.getFirstName()).isEqualTo("Admin");
    assertThat(adminUser.getLastName()).isEqualTo("Admin");
    assertThat(adminUser.getEmail()).isEqualTo("admin@localhost");
  }

  @Test
  public void paswordDefaultsToUserId() throws Exception {
    User adminUser = adminUser("admin", null, "", "", "").init();

    assertThat(adminUser.getId()).isEqualTo("admin");
    assertThat(adminUser.getPassword()).isEqualTo("admin");
  }

  private AdminUserProperty adminUser(String id, String password, String first, String last, String mail) {
    final AdminUserProperty adminUser = new AdminUserProperty();
    adminUser.setId(id);
    adminUser.setEmail(mail);
    adminUser.setFirstName(first);
    adminUser.setLastName(last);
    adminUser.setPassword(password);

    return adminUser;
  }

}

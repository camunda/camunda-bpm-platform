package org.camunda.bpm.security;

import static org.fest.assertions.api.Assertions.*;

import javax.inject.Inject;

import org.camunda.bpm.cycle.entity.User;
import org.camunda.bpm.cycle.security.IdentityHolder;
import org.camunda.bpm.security.MissingPrivilegesException;
import org.camunda.bpm.security.UnauthorizedException;
import org.camunda.bpm.security.UserIdentity;
import org.camunda.bpm.security.test.SecuredTestBean;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 *
 * @author nico.rehwaldt
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  locations = {"classpath:/spring/security-test-context.xml"}
)
public class SecurityAspectTest {

  @Inject
  private SecuredTestBean testBean;
  
  private static UserIdentity ADMIN_IDENTITY;
  private static UserIdentity USER_IDENTITY;
  
  @BeforeClass
  public static void beforeClass() {
    User admin = new User();
    admin.setAdmin(true);
    
    ADMIN_IDENTITY = new UserIdentity(admin);
    
    User user = new User();
    user.setAdmin(false);
    
    USER_IDENTITY = new UserIdentity(user);
  }
  
  @Before
  public void before() {
    IdentityHolder.setIdentity(null);
    SecuredTestBean.CALLED = false;
  }
  
  @Test
  public void shouldRestrictUnauthorizedAccess() {
    try {
      testBean.adminAccessFoo();
      fail("expected exception");
      
    } catch (UnauthorizedException e) {
      // expected
    }
    
    assertThat(SecuredTestBean.CALLED).isFalse();
  }
  
  @Test
  public void shouldRestrictUserAccess() {
    
    IdentityHolder.setIdentity(USER_IDENTITY);
    
    try {
      testBean.adminAccessFoo();
      fail("expected exception");
      
    } catch (MissingPrivilegesException e) {
      // expected
    }
    
    assertThat(SecuredTestBean.CALLED).isFalse();
  }
  
  @Test
  public void shouldAllowPrivildegedAccess() {
    
    IdentityHolder.setIdentity(ADMIN_IDENTITY);
    
    testBean.adminAccessFoo();
    
    assertThat(SecuredTestBean.CALLED).isTrue();
  }

  @Test
  public void shouldAllowAdminAccessToNormalResource() {
    
    IdentityHolder.setIdentity(ADMIN_IDENTITY);
    
    testBean.normalAccessFoo();
    
    assertThat(SecuredTestBean.CALLED).isTrue();
  }
  
  @Test
  public void shouldAllowUserAccessToNormalResource() {
    
    IdentityHolder.setIdentity(USER_IDENTITY);
    
    testBean.normalAccessFoo();
    
    assertThat(SecuredTestBean.CALLED).isTrue();
  }
  
  @Test
  public void shouldAllowAdminAccessToComplexSecuredMethod() {
    
    IdentityHolder.setIdentity(ADMIN_IDENTITY);
    
    testBean.complexRolesAllowed();
    
    assertThat(SecuredTestBean.CALLED).isTrue();
  }
  
  @Test
  public void shouldRestrictUserAccessToComplexSecuredMethod() {
    
    IdentityHolder.setIdentity(USER_IDENTITY);
    
    try {
      testBean.complexRolesAllowed();
      fail("expected exception");
      
    } catch (MissingPrivilegesException e) {
      // expected
    }
    
    assertThat(SecuredTestBean.CALLED).isFalse();
  }
}

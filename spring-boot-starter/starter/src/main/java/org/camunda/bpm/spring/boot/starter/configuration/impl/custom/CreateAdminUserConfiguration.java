package org.camunda.bpm.spring.boot.starter.configuration.impl.custom;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration;
import org.camunda.bpm.spring.boot.starter.property.AdminUserProperty;
import org.springframework.beans.BeanUtils;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Groups.CAMUNDA_ADMIN;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;

public class CreateAdminUserConfiguration extends AbstractCamundaConfiguration {

  private User adminUser;

  @PostConstruct
  void init() {
    adminUser = Optional.ofNullable(camundaBpmProperties.getAdminUser())
      .map(AdminUserProperty::init)
      .orElseThrow(fail("adminUser not configured!"));
  }

  @Override
  public void postProcessEngineBuild(final ProcessEngine processEngine) {
    requireNonNull(adminUser);

    final IdentityService identityService = processEngine.getIdentityService();
    final AuthorizationService authorizationService = processEngine.getAuthorizationService();

    if (userAlreadyExists(identityService, adminUser)) {
      return;
    }

    createUser(identityService, adminUser);

    // create group
    if (identityService.createGroupQuery().groupId(CAMUNDA_ADMIN).count() == 0) {
      Group camundaAdminGroup = identityService.newGroup(CAMUNDA_ADMIN);
      camundaAdminGroup.setName("camunda BPM Administrators");
      camundaAdminGroup.setType(Groups.GROUP_TYPE_SYSTEM);
      identityService.saveGroup(camundaAdminGroup);
    }

    // create ADMIN authorizations on all built-in resources
    for (Resource resource : Resources.values()) {
      if (authorizationService.createAuthorizationQuery().groupIdIn(CAMUNDA_ADMIN).resourceType(resource).resourceId(ANY).count() == 0) {
        AuthorizationEntity userAdminAuth = new AuthorizationEntity(AUTH_TYPE_GRANT);
        userAdminAuth.setGroupId(CAMUNDA_ADMIN);
        userAdminAuth.setResource(resource);
        userAdminAuth.setResourceId(ANY);
        userAdminAuth.addPermission(ALL);
        authorizationService.saveAuthorization(userAdminAuth);
      }
    }

    identityService.createMembership(adminUser.getId(), CAMUNDA_ADMIN);
    LOG.creatingInitialAdminUser(adminUser);
  }

  static boolean userAlreadyExists(IdentityService identityService, User adminUser) {
    final User existingUser = identityService.createUserQuery()
      .userId(adminUser.getId())
      .singleResult();
    if (existingUser != null) {
      LOG.skipAdminUserCreation(existingUser);
      return true;
    }
    return false;
  }

  static User createUser(final IdentityService identityService, final User adminUser) {
    User newUser = identityService.newUser(adminUser.getId());
    BeanUtils.copyProperties(adminUser, newUser);
    identityService.saveUser(newUser);
    return newUser;
  }

  @Override
  public String toString() {
    return createToString(Collections.singletonMap("adminUser", adminUser));
  }
}

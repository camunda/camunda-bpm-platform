package org.camunda.bpm.engine.test.authorization.util;

import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.engine.MissingAuthorization;
import org.camunda.bpm.engine.MissingAuthorization.Builder;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Filip Hrisafov
 */
public class MissingAuthorizationMatcher extends TypeSafeDiagnosingMatcher<MissingAuthorization> {

  private MissingAuthorization missing;

  private MissingAuthorizationMatcher(Authorization authorization) {
    this.missing = asMissingAuthorization(authorization);
  }

  public static Collection<MissingAuthorizationMatcher> asMatchers(List<Authorization> authorizations) {
    Collection<MissingAuthorizationMatcher> matchers = new ArrayList<MissingAuthorizationMatcher>(authorizations.size());
    for (Authorization authorization : authorizations) {
      matchers.add(new MissingAuthorizationMatcher(authorization));
    }
    return matchers;
  }

  private static MissingAuthorization asMissingAuthorization(Authorization authorization) {
    Builder builder = MissingAuthorization.builder();
    for (Permission permission : authorization.getPermissions(Permissions.values())) {
      if (permission != Permissions.NONE) {
        builder.permission(permission.getName());
        break;
      }
    }

    if (!Authorization.ANY.equals(authorization.getResourceId())) {
      // missing ANY authorizations are not explicitly represented in the error message
      builder.resourceId(authorization.getResourceId());
    }

    Resource resource = AuthorizationTestUtil.getResourceByType(authorization.getResourceType());
    builder.resource(resource.resourceName());
    return builder.build();
  }

  @Override
  protected boolean matchesSafely(MissingAuthorization item, Description mismatchDescription) {
    if (StringUtils.equals(missing.getResourceId(), item.getResourceId()) && StringUtils.equals(missing.getResourceType(), item.getResourceType())
        && StringUtils.equals(missing.getViolatedPermissionName(), item.getViolatedPermissionName())) {
      return true;
    }
    mismatchDescription.appendText("expected missing authorization: ").appendValue(missing).appendValue(" received: ").appendValue(item);
    return false;
  }

  @Override
  public void describeTo(Description description) {
    description.appendValue(missing);
  }
}

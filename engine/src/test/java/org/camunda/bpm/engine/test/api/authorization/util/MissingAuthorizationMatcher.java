package org.camunda.bpm.engine.test.api.authorization.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.MissingAuthorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * @author Filip Hrisafov
 */
public class MissingAuthorizationMatcher extends TypeSafeDiagnosingMatcher<MissingAuthorization> {

  private MissingAuthorization missing;

  private MissingAuthorizationMatcher(MissingAuthorization authorization) {
    this.missing = authorization;
  }

  public static Collection<Matcher<? super MissingAuthorization>> asMatchers(List<MissingAuthorization> missingAuthorizations) {
    Collection<Matcher<? super MissingAuthorization>> matchers =
        new ArrayList<Matcher<? super MissingAuthorization>>(missingAuthorizations.size());
    for (MissingAuthorization authorization : missingAuthorizations) {
      matchers.add(new MissingAuthorizationMatcher(authorization));
    }
    return matchers;
  }

  protected static MissingAuthorization asMissingAuthorization(Authorization authorization) {
    String permissionName = null;
    String resourceId = null;
    String resourceName = null;

    for (Permission permission : authorization.getPermissions(Permissions.values())) {
      if (permission != Permissions.NONE) {
        permissionName = permission.getName();
        break;
      }
    }

    if (!Authorization.ANY.equals(authorization.getResourceId())) {
      // missing ANY authorizations are not explicitly represented in the error message
      resourceId = authorization.getResourceId();
    }

    Resource resource = AuthorizationTestUtil.getResourceByType(authorization.getResourceType());
    resourceName = resource.resourceName();
    return new MissingAuthorization(permissionName, resourceName, resourceId);
  }

  public static List<MissingAuthorization> asMissingAuthorizations(List<Authorization> authorizations) {
    List<MissingAuthorization> missingAuthorizations = new ArrayList<MissingAuthorization>();
    for (Authorization authorization : authorizations) {
      missingAuthorizations.add(asMissingAuthorization(authorization));
    }
    return missingAuthorizations;
  }

  @Override
  protected boolean matchesSafely(MissingAuthorization item, Description mismatchDescription) {
    if (StringUtils.equals(missing.getResourceId(), item.getResourceId())
        && StringUtils.equals(missing.getResourceType(), item.getResourceType())
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

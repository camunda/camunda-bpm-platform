package org.camunda.bpm.security.test;

import org.camunda.bpm.cycle.entity.User;
import org.camunda.bpm.security.UserLookup;

/**
 *
 * @author nico.rehwaldt
 */
public class FakeUserLookup implements UserLookup {

  @Override
  public User findByName(String userName) {
    return null;
  }
}

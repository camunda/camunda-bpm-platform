package com.camunda.fox.security.test;

import com.camunda.fox.cycle.entity.User;
import com.camunda.fox.security.UserLookup;

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

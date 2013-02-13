package com.camunda.fox.security;

import com.camunda.fox.cycle.entity.User;

/**
 *
 * @author nico.rehwaldt
 */
public interface UserLookup {

  public User findByName(String userName);
}

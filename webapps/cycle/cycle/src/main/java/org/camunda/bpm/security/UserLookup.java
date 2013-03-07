package org.camunda.bpm.security;

import org.camunda.bpm.cycle.entity.User;

/**
 *
 * @author nico.rehwaldt
 */
public interface UserLookup {

  public User findByName(String userName);
}

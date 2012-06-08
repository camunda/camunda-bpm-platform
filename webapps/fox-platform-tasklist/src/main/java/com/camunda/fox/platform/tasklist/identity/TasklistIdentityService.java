package com.camunda.fox.platform.tasklist.identity;

import java.util.List;

/**
 * This interface provides an example API for an identity service. 
 * 
 * @author Nils Preusker - nils.preusker@camunda.com
 */
public interface TasklistIdentityService {

  // ...
  
  /**
   * check the user with the given id and password and throws an exception
   * if the user doesn't exist or the password is invalid.
   */
  public void authenticateUser(String userId, String password);

  /**
   * Returns a list of group ids, representing the groups the user is a member
   * of.
   * 
   * @param userId
   *          the id of the user who's groups should be searched for
   * @return a list of group ids, representing the groups the user is a member
   *         of
   */
  public List<String> getGroupsByUserId(String userId);

  /**
   * Returns the colleagues of the user with the given id.
   * 
   * @param userId
   *          the id of the user who's colleagues should be returned
   * @return the colleagues of the given user
   */
  public List<User> getColleaguesByUserId(String userId);

  // ...

}

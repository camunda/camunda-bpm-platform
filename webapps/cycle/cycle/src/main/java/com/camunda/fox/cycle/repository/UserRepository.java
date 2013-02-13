package com.camunda.fox.cycle.repository;


import javax.persistence.NoResultException;
import org.springframework.stereotype.Repository;

import com.camunda.fox.cycle.entity.User;
import com.camunda.fox.security.UserLookup;

/**
 *
 * @author nico.rehwaldt
 */
@Repository
public class UserRepository extends AbstractRepository<User> implements UserLookup {

  public boolean isNameAvailable(String name) {
    return em.createQuery("SELECT COUNT(u) FROM User u WHERE u.name = :name", Long.class)
             .setParameter("name", name)
             .getSingleResult() == 0;
  }

  /**
   * Return user with the given name
   * 
   * @param name
   * @return 
   */
  public User findByName(String name) {
    try {
      return em.createQuery("SELECT u FROM User u WHERE u.name = :name", User.class)
               .setParameter("name", name)
               .getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }
}

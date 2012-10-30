package com.camunda.fox.cycle.repository;


import org.springframework.stereotype.Repository;

import com.camunda.fox.cycle.entity.User;

/**
 *
 * @author nico.rehwaldt
 */
@Repository
public class UserRepository extends AbstractRepository<User> {

  public boolean isNameValid(String name) {
    return em.createQuery("SELECT COUNT(u) FROM User u WHERE u.name = :name", Long.class)
             .setParameter("name", name)
             .getSingleResult() == 0;
  }
}

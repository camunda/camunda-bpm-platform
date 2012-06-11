package com.camunda.fox.tasklist.identity.ldap;

import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

public abstract class LdapCommand<T> {

  public abstract T execute(DirContext ctx) throws Exception;
  
  protected SearchControls getSimpleSearchControls() {
    SearchControls searchControls = new SearchControls();
    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    searchControls.setTimeLimit(30000);
    return searchControls;
  }
}

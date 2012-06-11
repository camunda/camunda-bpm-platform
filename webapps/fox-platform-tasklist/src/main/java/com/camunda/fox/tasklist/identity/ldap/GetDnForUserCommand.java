package com.camunda.fox.tasklist.identity.ldap;

import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

public class GetDnForUserCommand extends LdapCommand<String> {

  private String userId;

  public GetDnForUserCommand(String userId) {
    this.userId = userId;
  }

  @Override
  public String execute(DirContext ctx) throws Exception {
    NamingEnumeration< ? > userIdEnum = ctx.search(LdapConstants.getBaseDn(), "(" + LdapConstants.getUserIdAttribute() + "="+userId+")", getSimpleSearchControls());
    if (!userIdEnum.hasMore()) {
      // dont' do this in production, it is best practice to not let the user
      // know if user or password is the problem
      throw new IllegalArgumentException("User '" + userId + "' not found");
    }
    SearchResult userSearchResult = (SearchResult) userIdEnum.next();
    return userSearchResult.getNameInNamespace();
  }
}

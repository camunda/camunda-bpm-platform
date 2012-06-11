package com.camunda.fox.tasklist.identity.ldap;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;


public class GetGroupsForUserCommand extends LdapCommand<List<String>> {

  private String userId;

  public GetGroupsForUserCommand(String userId) {
    this.userId = userId;
  }
  
  @Override
  public List<String> execute(DirContext ctx) throws Exception {
    List<String> groupIds = new ArrayList<String>();
    
    String dn = new GetDnForUserCommand(userId).execute(ctx);
    
    NamingEnumeration< ? > groupSearchResults = ctx.search(LdapConstants.getBaseDn(), "(member="+dn+")", getSimpleSearchControls());
    while (groupSearchResults.hasMore()) {
      SearchResult groupSearchResult = (SearchResult) groupSearchResults.next();
      String groupName = groupSearchResult.getAttributes().get(LdapConstants.getGroupNameAttribute()).get().toString();
      groupIds.add(groupName);
    }
    
    return groupIds;
  }
}

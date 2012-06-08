package com.camunda.fox.platform.tasklist.identity.ldap;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import com.camunda.fox.platform.tasklist.identity.User;


public class GetColleaguesForUserCommand extends LdapCommand<List<User>> {

  private String userId;

  public GetColleaguesForUserCommand(String userId) {
    this.userId = userId;
  }
  
  @Override
  public List<User> execute(DirContext ctx) throws Exception {
    List<User> colleagues = new ArrayList<User>();
    
    NamingEnumeration< ? > userIdEnum = ctx.search(LdapConstants.getBaseDn(), "(" + LdapConstants.getUserIdAttribute() + "="+userId+")", getSimpleSearchControls());
    if (!userIdEnum.hasMore()) {
      // dont' do this in production, it is best practice to not let the user
      // know if user or password is the problem
      throw new IllegalArgumentException("User '" + userId + "' not found");
    }
    SearchResult userSearchResult = (SearchResult) userIdEnum.next();
    String rootPath = userSearchResult.getNameInNamespace().substring(userSearchResult.getNameInNamespace().indexOf(",")+1);
    
    NamingEnumeration< ? > namingEnum = ctx.search(rootPath, "(objectclass=person)", getSimpleSearchControls());
    while (namingEnum.hasMore()) {
      SearchResult searchResult = (SearchResult) namingEnum.next();
      Attributes attrs = searchResult.getAttributes();
      if (!attrs.get("uid").toString().equals(userId)) {
        colleagues.add(new User(attrs.get("uid").get().toString(), attrs.get("cn").get().toString(), attrs.get("sn").get().toString()));
      }        
    }
    
    return colleagues;
  }
}

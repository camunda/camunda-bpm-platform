import com.camunda.fox.platform.tasklist.identity.ldap.LdapIdentityServiceImpl;



public class QueryStarter {
  public static void main(String[] args) {
    LdapIdentityServiceImpl ldap = new LdapIdentityServiceImpl();
    ldap.authenticateUser("kermit", "kermit");
    System.out.println(ldap.getGroupsByUserId("kermit"));
    System.out.println(ldap.getColleaguesByUserId("gonzo"));
    System.out.println("YEAH");
  }
}

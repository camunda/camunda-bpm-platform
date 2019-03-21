package org.camunda.bpm.engine.test.api.passwordpolicy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.pwpolicy.PasswordPolicy;
import org.camunda.bpm.engine.pwpolicy.PasswordPolicyRule;

/**
 * Implementation for testing purposes.
 * 
 * @author Miklas Boskamp
 */
public class CustomPasswordPolicyImpl implements PasswordPolicy {

  public static List<String> passwordBlacklist = Arrays.asList("password", "hello", "qwerty");

  private List<PasswordPolicyRule> rules;

  public CustomPasswordPolicyImpl() {
    rules = new ArrayList<PasswordPolicyRule>();
    rules.add(getBlackListStringRule());
    rules.add(getNoConsecutiveDigitRule());
  }

  @Override
  public List<PasswordPolicyRule> getRules() {
    return rules;
  }

  private PasswordPolicyRule getBlackListStringRule() {
    return new PasswordPolicyRule() {

      @Override
      public String getPlaceholder() {
        return "BLACKLIST";
      }

      @Override
      public Map<String, String> getParameter() {
        Map<String, String> param = new HashMap<String, String>();
        param.put("blacklist", passwordBlacklist.toString());
        return param;
      }

      @Override
      public boolean execute(String password) {
        for (String string : passwordBlacklist) {
          if (password.contains(string)) {
            return false;
          }
        }
        return true;
      }
    };
  }

  private PasswordPolicyRule getNoConsecutiveDigitRule() {
    return new PasswordPolicyRule() {
      @Override
      public String getPlaceholder() {
        return "NO_CONSECUTIVE_DIGITS";
      }

      @Override
      public Map<String, String> getParameter() {
        return null;
      }

      @Override
      public boolean execute(String password) {
        for (int i = 0; i < password.length() - 1; i++) {
          char first = password.charAt(i);
          char second = password.charAt(i + 1);
          if (Character.isDigit(first) && Character.isDigit(second)) {
            return false;
          }
        }
        return true;
      }
    };
  }

}

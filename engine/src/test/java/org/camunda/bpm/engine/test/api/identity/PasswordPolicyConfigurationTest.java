package org.camunda.bpm.engine.test.api.identity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.nullValue;

import org.camunda.bpm.engine.identity.PasswordPolicy;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.identity.DefaultPasswordPolicyImpl;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class PasswordPolicyConfigurationTest {

  protected static ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  protected static ProcessEngineConfigurationImpl processEngineConfiguration;

  static PasswordPolicy passwordPolicyDefaultSetting;
  static Boolean passwordPolicyDisabledDefaultSetting;

  @Before
  public void init() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();

    if (passwordPolicyDisabledDefaultSetting == null) {
      // save initial configuration
      passwordPolicyDefaultSetting = processEngineConfiguration.getPasswordPolicy();
      passwordPolicyDisabledDefaultSetting = processEngineConfiguration.isDisablePasswordPolicy();
    } else {
      // restore initial configuration
      processEngineConfiguration.setPasswordPolicy(passwordPolicyDefaultSetting);
      processEngineConfiguration.setDisablePasswordPolicy(passwordPolicyDisabledDefaultSetting);
    }
  }

  @AfterClass
  public static void tearDown() {
    processEngineConfiguration.setPasswordPolicy(passwordPolicyDefaultSetting).setDisablePasswordPolicy(passwordPolicyDisabledDefaultSetting);
  }

  @Test
  public void testInitialConfiguration() {
    // given initial configuration

    // when
    processEngineConfiguration.initPasswordPolicy();

    // then
    assertThat(processEngineConfiguration.getPasswordPolicy(), nullValue());
    assertThat(processEngineConfiguration.isDisablePasswordPolicy(), is(true));
  }

  @Test
  public void testAutoConfigurationDefaultPasswordPolicy() {
    // given
    processEngineConfiguration.setDisablePasswordPolicy(false);

    // when
    processEngineConfiguration.initPasswordPolicy();

    // then
    assertThat(processEngineConfiguration.isDisablePasswordPolicy(), is(false));
    assertThat(processEngineConfiguration.getPasswordPolicy(), is(instanceOf(DefaultPasswordPolicyImpl.class)));
  }

  @Test
  public void testFullPasswordPolicyConfiguration() {
    // given
    processEngineConfiguration.setDisablePasswordPolicy(false);
    processEngineConfiguration.setPasswordPolicy(new DefaultPasswordPolicyImpl());

    // when
    processEngineConfiguration.initPasswordPolicy();

    // then
    assertThat(processEngineConfiguration.isDisablePasswordPolicy(), is(false));
    assertThat(processEngineConfiguration.getPasswordPolicy(), is(instanceOf(DefaultPasswordPolicyImpl.class)));
  }
}
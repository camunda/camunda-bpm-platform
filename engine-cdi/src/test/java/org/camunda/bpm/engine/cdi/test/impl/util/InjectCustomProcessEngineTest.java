package org.camunda.bpm.engine.cdi.test.impl.util;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import org.camunda.bpm.engine.cdi.test.impl.beans.InjectedProcessEngineBean;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.*;
import org.junit.runner.RunWith;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@RunWith(Arquillian.class)
public class InjectCustomProcessEngineTest {

  @Deployment
  public static JavaArchive createDeployment() {

    return ShrinkWrap.create(JavaArchive.class)
      .addPackages(true, "org.camunda.bpm.engine.cdi")
      .addAsManifestResource("org/camunda/bpm/engine/cdi/test/impl/util/beans.xml", "beans.xml");
  }

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule("org/camunda/bpm/engine/cdi/test/impl/util/camunda.cfg.xml", true);

  @Before
  public void init() {
    if(BpmPlatform.getProcessEngineService().getDefaultProcessEngine() == null) {
      RuntimeContainerDelegate.INSTANCE.get().registerProcessEngine(processEngineRule.getProcessEngine());
    }
  }

  @After
  public void tearDownCdiProcessEngineTestCase() throws Exception {
    RuntimeContainerDelegate.INSTANCE.get().unregisterProcessEngine(processEngineRule.getProcessEngine());
  }

  @Test
  public void testProcessEngineInject() {
    //given only custom engine exist

    //when TestClass is created
    InjectedProcessEngineBean testClass = ProgrammaticBeanLookup.lookup(InjectedProcessEngineBean.class);
    Assert.assertNotNull(testClass);

    //then custom engine is injected
    Assert.assertEquals("myCustomEngine", testClass.processEngine.getName());
  }
}

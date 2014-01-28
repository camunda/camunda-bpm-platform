package org.camunda.bpm.integrationtest.deployment.war.beans;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.scripting.ScriptBindingsFactory;
import org.camunda.bpm.engine.impl.scripting.ScriptingEngines;

public class GroovyProcessEnginePlugin implements ProcessEnginePlugin {

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {

  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    processEngineConfiguration.setScriptingEngines(
        new ScriptingEngines(
            new ScriptBindingsFactory(processEngineConfiguration.getResolverFactories())
        )
    );
  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {

  }
}

package org.camunda.bpm.qa.performance.engine.junit;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.consulting.snippet.engine_plugin_external_task_listener.ExternalTaskListenerCommandInterceptor;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.interceptor.CommandInterceptor;

public class ExternalTaskRecorderProcessEnginePlugin implements ProcessEnginePlugin {

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    List<CommandInterceptor> postCommandInterceptors = processEngineConfiguration.getCustomPostCommandInterceptorsTxRequired();
    if (postCommandInterceptors == null) {
      postCommandInterceptors = new ArrayList<>();
      processEngineConfiguration.setCustomPostCommandInterceptorsTxRequired(postCommandInterceptors);
    }
    postCommandInterceptors.add(new ExternalTaskListenerCommandInterceptor(new ExternalTaskRecorder()));
  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {

  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {

  }

}

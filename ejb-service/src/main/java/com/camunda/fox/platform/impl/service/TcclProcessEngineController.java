package com.camunda.fox.platform.impl.service;

import com.camunda.fox.platform.impl.service.util.Tccl;
import com.camunda.fox.platform.impl.service.util.Tccl.Operation;
import com.camunda.fox.platform.spi.ProcessEngineConfiguration;

/**
 * 
 * @author Daniel Meyer
 */
public class TcclProcessEngineController extends ProcessEngineController {

  public TcclProcessEngineController(ProcessEngineConfiguration processEngineConfiguration) {
    super(processEngineConfiguration);
  }

  public synchronized void start() {
    Tccl.runUnderClassloader(new Operation<Void>() {
      public Void run() {
        TcclProcessEngineController.super.start();
        return null;
      }
    }, ProcessEngineController.class.getClassLoader());
  }

}

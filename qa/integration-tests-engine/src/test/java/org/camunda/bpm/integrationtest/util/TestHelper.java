package org.camunda.bpm.integrationtest.util;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;


public abstract class TestHelper {
  
  public final static String PROCESS_XML = 
          "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"  targetNamespace=\"Examples\"><process id=\"PROCESS_KEY\" isExecutable=\"true\" /></definitions>";
    
  public static Asset getStringAsAssetWithReplacements(String string, String[][] replacements) {

    for (String[] replacement : replacements) {
      string = string.replaceAll(replacement[0], replacement[1]);
    }

    return new ByteArrayAsset(string.getBytes());

  }
  
  public static Asset[] generateProcessAssets(int amount) {
    
    Asset[] result = new Asset[amount];
    
    for (int i = 0; i < result.length; i++) {
      result[i] = getStringAsAssetWithReplacements(PROCESS_XML, new String[][]{new String[]{"PROCESS_KEY","process-"+i}});
    }
    
    return result;

  }

}

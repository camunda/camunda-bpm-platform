package com.camunda.fox.cycle.service.roundtrip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import com.camunda.fox.cycle.exception.CycleException;
import com.camunda.fox.cycle.service.roundtrip.transform.XsltTransformer;
import com.camunda.fox.cycle.util.IoUtil;

/**
 * Utility Class providing 
 * <ul>
 *   <li>replacement of bpmn element IDs with developer-friendly IDs</li>
 *   <li>extraction of an executable pool from a bpmn collaboration</li>
 * </ul> 
 * This class works on the xml representation of a bpmn process model provided as a String.
 * 
 * @author Daniel Meyer
 */
public class BpmnProcessModelUtil {
  
  public static final String UTF_8 = "UTF-8";

  protected XsltTransformer transformer = XsltTransformer.instance();
  
  /**
   * Replaces the bpmn element IDs in a process model with developer friendly
   * IDs.
   * 
   * @param sourceModel
   *          a bpmn20 process model in XML representation
   * @param processEnginePoolId
   *          allows to provide a custom ID that is used for the first <process .../> element with 
   *          'isExecutable="true"' that is found by the transformer           
   * @return the process model such that the element ids are replaced with    
   *         developer-friendly IDs
   */
  public String replaceDeveloperFriendlyIds(String sourceModel, String processEnginePoolId) {
    
    ByteArrayInputStream inputStream = new ByteArrayInputStream(getBytesFromString(sourceModel));
    ByteArrayOutputStream resultModel = null;
    
    try {
      
      resultModel = transformer.developerFriendly(inputStream, processEnginePoolId, true);
      return getStringFromBytes(resultModel.toByteArray());
      
    } finally {
      IoUtil.closeSilently(inputStream);
      IoUtil.closeSilently(resultModel);
    }
    
  }

  /**
   * Replaces the bpmn element IDs in a process model with developer friendly
   * IDs.
   * 
   * @param sourceModel
   *          a bpmn20 process model in XML representation
   * @return the process model such that the element ids are replaced with    
   *         developer-friendly IDs
   */
  public String replaceDeveloperFriendlyIds(String sourceModel) {    
    return replaceDeveloperFriendlyIds(sourceModel, "Process_Engine");
  }
  
  /**
   * Takes a bpmn process model in XML representation as input. Removes all pools 
   * except for a single executable pool (property 'isExecutable="true").
   * 
   * NOTE: assumes that the process model contains a single executable pool.
   * 
   * @param sourceModel
   *         a bpmn20 process model in XML representation
   * @return the process model containing the extracted pool
   * 
   */
  public String extractExecutablePool(String sourceModel) {
    
    ByteArrayInputStream inputStream = new ByteArrayInputStream(getBytesFromString(sourceModel));
    ByteArrayOutputStream resultModel = null;
    
    try {
      
      resultModel = transformer.poolExtraction(inputStream, true);
      return getStringFromBytes(resultModel.toByteArray());
      
    } finally {
      IoUtil.closeSilently(inputStream);
      IoUtil.closeSilently(resultModel);
    }
    
  }
  
  
  // utils ////////////////////////////////////////////////////
  
  protected byte[] getBytesFromString(String string) {
    try {
      return string.getBytes(UTF_8);
    } catch (UnsupportedEncodingException e) {
      throw new CycleException("Unable to get bytes from source model", e);
    }
  }

  protected String getStringFromBytes(byte[] byteArray) {
    try {
      return new String(byteArray, UTF_8);
    } catch (UnsupportedEncodingException e) {
      throw new CycleException("Unable to get bytes from result model", e);
    }
  }

}

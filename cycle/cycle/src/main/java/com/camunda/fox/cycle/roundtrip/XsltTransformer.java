package com.camunda.fox.cycle.roundtrip;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.camunda.fox.cycle.exception.CycleException;
import com.camunda.fox.cycle.util.ExceptionUtil;

public class XsltTransformer {

  private static final String NET_SF_SAXON_TRANSFORMER_FACTORY_IMPL = "net.sf.saxon.TransformerFactoryImpl";
  private static Logger log = Logger.getLogger(XsltTransformer.class.getSimpleName());
  
  private static XsltTransformer instance = new XsltTransformer();

  private TransformerFactory transformerFactory;
  
  private XsltTransformer() { }

  /** 
   * Return instance of this transformer
   */ 
  public static XsltTransformer instance() {
    return instance;
  }

  private ErrorListener errorListener = new ErrorListener() {
    @Override
    public void warning(TransformerException exception) throws TransformerException {
      log.log(Level.FINE, exception.getMessageAndLocation());
    }

    @Override
    public void fatalError(TransformerException exception) throws TransformerException {
      throw new RuntimeException(exception);
    }

    @Override
    public void error(TransformerException exception) throws TransformerException {
      throw new RuntimeException(exception);
    }
  };

  public ByteArrayOutputStream developerFriendly(InputStream bpmn, String enginePoolId, boolean keepLanes) {
    try {
      // need thread safety here: cannot simply cache transformer!
      Transformer t = createDeveloperFriendlyTransformer();
      t.setParameter("keepLanes", "" + keepLanes);

      if (enginePoolId != null) {
        t.setParameter("externalPoolId", enginePoolId);
      }

      ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
      t.transform(new StreamSource(bpmn), new StreamResult(resultStream));
      
      return resultStream;
    } catch (Exception e) {
      throw new RuntimeException("Could not make BPMN developerfriendly", e);
    }
  }

  public ByteArrayOutputStream poolExtraction(InputStream bpmn, boolean keepLanes) {
    return poolExtraction(bpmn, keepLanes, null, null);
  }

  public ByteArrayOutputStream poolExtraction(InputStream bpmn, boolean keepLanes, String offsetX, String offsetY) {
    try {
      // need thread safety here: cannot simply cache transformer!
      Transformer t = createEnginePoolTransformer();
      t.setParameter("keepLanes", "" + keepLanes);

      if (offsetX != null) {
        t.setParameter("offsetX", offsetX);
      }

      if (offsetY != null) {
        t.setParameter("offsetY", offsetY);
      }

      ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
      t.transform(new StreamSource(bpmn), new StreamResult(resultStream));
      return resultStream;
    } catch (Exception e) {
      Throwable t = ExceptionUtil.getRootCause(e);
      throw new CycleException("Could not extract pool from BPMN: " + t.getMessage(), e);
    }
  }
  
  private Transformer createTransformer(String xsl) throws TransformerConfigurationException {
    
    // Input stream for the transformation stylesheet
    InputStream is = getClass().getResourceAsStream(xsl);
    if (is == null) {
      throw new IllegalArgumentException("XSL document " + xsl + " could not be locate");
    }
    return getTransformerFactory().newTransformer(new StreamSource(is));
  }
  
  Transformer createEnginePoolTransformer() throws TransformerConfigurationException {
    return createTransformer("poolExtraction.xsl");
  }

  Transformer createDeveloperFriendlyTransformer() throws TransformerConfigurationException {
    return createTransformer("developerFriendly.xsl");
  }

  private TransformerFactory getTransformerFactory() {
    if (transformerFactory == null) {
      transformerFactory = TransformerFactory.newInstance(NET_SF_SAXON_TRANSFORMER_FACTORY_IMPL, null);
      transformerFactory.setErrorListener(errorListener);
    }
    
    return transformerFactory;
  }
}

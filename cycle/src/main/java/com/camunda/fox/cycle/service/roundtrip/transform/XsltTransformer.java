package com.camunda.fox.cycle.service.roundtrip.transform;

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

public class XsltTransformer {
	
	private static final String NET_SF_SAXON_TRANSFORMER_FACTORY_IMPL = "net.sf.saxon.TransformerFactoryImpl";

	private Logger log = Logger.getLogger(XsltTransformer.class.getSimpleName());
	
	private static XsltTransformer instance = new XsltTransformer();
	
	private Transformer poolEngineTransfomer;
	private Transformer developerFriendlyTransformer;
	
	
	private XsltTransformer() {}
	
	public static XsltTransformer instance(){
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
		try{
			getDeveloperFriendlyTransformer().clearParameters();
			getDeveloperFriendlyTransformer().setParameter("keepLanes", "" + keepLanes);
			
			if (enginePoolId != null){
				getDeveloperFriendlyTransformer().setParameter("externalPoolId", enginePoolId);
			}
			
			ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
			getDeveloperFriendlyTransformer().transform(new StreamSource(bpmn), new StreamResult(
					resultStream));
			return resultStream;
		}catch (Exception e) {
			throw new RuntimeException("Could not make BPMN developerfriendly", e);
		}
	}
	
	public ByteArrayOutputStream poolExtraction(InputStream bpmn, boolean keepLanes){
		return poolExtraction(bpmn, keepLanes, null, null);
	}
	
	public ByteArrayOutputStream poolExtraction(InputStream bpmn, boolean keepLanes, String offsetX, String offsetY) {
		try{
			// needed, cause we are not recreating the template
			getPoolEngineTransformer().clearParameters();
			
			getPoolEngineTransformer().setParameter("keepLanes", "" + keepLanes);
			
			if (offsetX != null){
				getPoolEngineTransformer().setParameter("offsetX", offsetX);
			}
			
			if (offsetY != null){
				getPoolEngineTransformer().setParameter("offsetY", offsetY);
			}
			
			ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
			getPoolEngineTransformer().transform(new StreamSource(bpmn), new StreamResult(
					resultStream));
			return resultStream;
		} catch (Exception e) {
			throw new RuntimeException("Could not extract pool from BPMN", e);
		}
	}
	
	private Transformer getPoolEngineTransformer() throws TransformerConfigurationException{
		if (this.poolEngineTransfomer == null){
			TransformerFactory tFactory = TransformerFactory.newInstance(NET_SF_SAXON_TRANSFORMER_FACTORY_IMPL, null);
			tFactory.setErrorListener(errorListener);
			InputStream poolExtractionStyleSheet = getClass().getResourceAsStream("poolExtraction.xsl");
			poolEngineTransfomer = tFactory.newTransformer(new StreamSource(poolExtractionStyleSheet));
		}
		return this.poolEngineTransfomer;
	}
	
	private Transformer getDeveloperFriendlyTransformer() throws TransformerConfigurationException{
		if (this.developerFriendlyTransformer == null){
			TransformerFactory tFactory = TransformerFactory.newInstance(NET_SF_SAXON_TRANSFORMER_FACTORY_IMPL, null);
			tFactory.setErrorListener(errorListener);
			InputStream devStyleSheet = getClass().getResourceAsStream("developerFriendly.xsl");
			developerFriendlyTransformer = tFactory.newTransformer(new StreamSource(devStyleSheet));
		}
		return this.developerFriendlyTransformer;
	}
}

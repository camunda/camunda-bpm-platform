package org.camunda.bpm.cycle.connector.signavio;


import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.cycle.connector.ConnectorNode;
import org.camunda.bpm.cycle.connector.ConnectorNodeType;
import org.camunda.bpm.cycle.connector.signavio.SignavioConnector;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.camunda.bpm.cycle.roundtrip.BpmnProcessModelUtil;
import org.camunda.bpm.cycle.roundtrip.XsltTransformer;
import org.camunda.bpm.cycle.util.BpmnNamespaceContext;
import org.camunda.bpm.cycle.util.IoUtil;
import org.camunda.bpm.cycle.util.XmlUtil;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParser;
import org.camunda.bpm.engine.impl.cfg.BpmnParseFactory;
import org.camunda.bpm.engine.impl.cfg.DefaultBpmnParseFactory;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/test/signavio-connector-xml-config.xml" })
public class SignavioConnectorIT {

  /**
   * Files generated for each test are: 
   * initial-raw ->
   * technical -> 
   * changed-technical (only when you replace something) ->
   * before-deploy-technical (only when you deploy to engine) ->
   * merge-source-technical ->
   * merge-source-business ->
   * merge-result ->
   * actual-raw ->
   * expected-raw ->
   * xml-diff
   */
  
  private static final String INITIAL_RAW_BPMN20_XML = "initial-raw.bpmn";
  private static final String TECHNICAL_BPMN20_XML = "technical.bpmn";
  private static final String CHANGED_TECHNICAL_BPMN20_XML = "changed-technical.bpmn";
  private static final String BEFORE_DEPLOY_TECHNICAL_BPMN20_XML = "before-deploy-technical.bpmn";
  private static final String ACTUAL_RAW_BPMN20_XML = "actual-raw.bpmn";
  private static final String EXPECTED_RAW_BPMN20_XML = "expected-raw.bpmn";
  private static final String XML_DIFF_TXT = "xml-diff.txt";
  
  private static final String MODEL_FOLDER = "models";
  private static final String DEBUG_DIR = "target/failsafe-reports";
  private static final String TEST_RESOURCES_PATH = "src/test/resources/" + MODEL_FOLDER + "/";
  
  private static final boolean OVERWRITE_EXPECTED_BPMN_FILES = false;

  private static final String[] testBpmnModels = new String[] { "MyProcess.bpmn", "SimpleProcurementExample.bpmn", "TwitterDemoProcess.bpmn",
      "TwitterDemoProcess-business-rule-task.bpmn", "TwitterDemoProcess-business-rule-task.dev-friendly.2011-09-01.bpmn" };
  
  private BpmnProcessModelUtil bpmnProcessModelUtil = new BpmnProcessModelUtil();
  
  @Inject
  private ProcessEngineConfigurationImpl processEngineConfiguration;
  @Inject
  private SignavioConnector signavioConnector;

  @Before
  public void setUp() throws Exception {
    XMLUnit.setIgnoreWhitespace(true);
    XMLUnit.setIgnoreAttributeOrder(true);
    XMLUnit.setXpathNamespaceContext(new SimpleNamespaceContext(new BpmnNamespaceContext().getNamespaces()));
    
    ConnectorConfiguration config = getSignavioConnector().getConfiguration();
    getSignavioConnector().init(config);
    getSignavioConnector().login(config.getGlobalUser(), config.getGlobalPassword());
  }
  
  @After
  public void tearDown() throws Exception {
    getSignavioConnector().dispose();
  }

  @Test
  public void testActivitiExtensionsImportExport() throws Exception {
    final String ACT_ELEMENT_COUNT = "count(//*[namespace-uri() = 'http://activiti.org/bpmn'])";
    final String ACT_ATTRIBUTE_COUNT = "count(//@*[namespace-uri() = 'http://activiti.org/bpmn'])";

    for (String model : readModels()){
      String expectedXml = model;
      String expectedElementCount = XmlUtil.getXPathResult(ACT_ELEMENT_COUNT, new InputSource(IOUtils.toInputStream(expectedXml, "UTF-8")));
      String expectedAttributeCount = XmlUtil.getXPathResult(ACT_ATTRIBUTE_COUNT, new InputSource(IOUtils.toInputStream(expectedXml, "UTF-8")));

      ConnectorNode importedNode = getSignavioConnector().importContent(getSignavioConnector().getPrivateFolder(), expectedXml);

      InputStream actualXmlInputStream = getSignavioConnector().getContent(importedNode);
      String actualXml = IOUtils.toString(actualXmlInputStream, "UTF-8");
      actualXmlInputStream.close();
      
      getSignavioConnector().deleteNode(importedNode, null);
      assertXpathEvaluatesTo(expectedElementCount, ACT_ELEMENT_COUNT, actualXml);
      assertXpathEvaluatesTo(expectedAttributeCount, ACT_ATTRIBUTE_COUNT, actualXml);
    }
  }
  
  @Test
  public void testImportTechnicalModel() throws Exception {
    bpmnSimplePoolExtractionRoundtripWithDevFriendlyEngineDeploy("MyProcess_executable.bpmn");
  }
  
  @Test
  public void testRoundtripWithMessageEvents() throws Exception {
    bpmnSimplePoolExtractionRoundtripWithDevFriendlyEngineDeploy("testRoundtripWithMessageEvents.sgx",
            "<serviceTask\\sid=\"(test(_\\d*))\"\\sname=\"test\">",
            "<serviceTask id=\"testTwitter\" name=\"testTwitter\" activiti:class=\"org.camunda.bpm.demo.twitter.TweetContentDelegate\" >");
  }
  
  @Test
  public void test_HEMERA_1319() throws Exception {
    bpmnSimplePoolExtractionRoundtripWithDevFriendlyEngineDeploy("HEMERA-1319.sgx",
            "<serviceTask\\sid=\"(Task(_\\d*))\">",
            "<serviceTask id=\"Task_abc\" name=\"Task_abc\" activiti:class=\"org.camunda.bpm.demo.pdf.SavePdfDelegate\" >");
  }
  
  @Test
  public void test_HEMERA_1610() throws Exception {
    bpmnSimplePoolExtractionRoundtripWithDevFriendlyEngineDeploy("HEMERA-1610.sgx",
      "<serviceTask\\sid=\"(PDF_in_SVN_ablegen(_\\d*))\"\\sname=\"PDF\\sin\\sSVN\\sablegen\">",
      "<serviceTask id=\"PDF_in_SVN_ablegen_abc\" name=\"PDF in SVN ablegen\" activiti:class=\"org.camunda.bpm.demo.pdf.SavePdfDelegate\" >");
  }
  
  @Test
  public void test_HEMERA_1348_RoundtripWithMessageEventsAdvanced() throws Exception {
    bpmnSimplePoolExtractionRoundtripWithDevFriendlyEngineDeploy("HEMERA-1348.sgx");
  }

  @Test
  public void test_HEMERA_1791_changeEngineProcessId() throws Exception {
    bpmnSimplePoolExtractionRoundtripWithDevFriendlyEngineDeploy("HEMERA-1791-Collaboration.bpmn",
            "<process\\sid=\"(HEMERA-1791-Collaboration_)\"\\sisExecutable=\"true\"\\s*name=\"Process\\sEngine\">",
            "<process id=\"changed-processid-HEMERA-1791-blub-Collaboration_\" isClosed=\"true\" isExecutable=\"true\" name=\"Process Engine\">");
  }
  
  @Test
  public void test_HEMERA_1791_changeEngineProcessName() throws Exception {
    bpmnSimplePoolExtractionRoundtripWithDevFriendlyEngineDeploy("HEMERA-1791-Collaboration.bpmn",
            "<process\\sid=\"HEMERA-1791-Collaboration_\"\\sisExecutable=\"true\"\\s*name=\"Process\\sEngine\">",
            "<process id=\"HEMERA-1791-Collaboration_\" isExecutable=\"true\" name=\"changed-processname-Process Engine\">");
  }

  @Test
  public void test_HEMERA_1791_changeEngineProcessIdAndName() throws Exception {
    bpmnSimplePoolExtractionRoundtripWithDevFriendlyEngineDeploy("HEMERA-1791-Collaboration.bpmn",
            "<process\\sid=\"(HEMERA-1791-Collaboration_)\"\\sisExecutable=\"true\"\\s*name=\"Process\\sEngine\">",
            "<process id=\"changed-processid-HEMERA-1791-blub-Collaboration_\" isExecutable=\"true\" name=\"change-processname-Process Engine\">");
  }
  
  @Test
  public void test_HEMERA_2219() throws Exception {
    String roundtripResult = bpmnPoolExtractionRoundtrip("HEMERA-2219.sgx", 
            false,
            null,
            null,
            true,
            "HEMERA-2219-import.bpmn");
    
      assertXpathEvaluatesTo("2", "count(//bpmn:message)", roundtripResult);
      assertXpathEvaluatesTo("2", "count(//bpmn:error)", roundtripResult);
  }
  
  @Test
  public void test_HEMERA_2379() throws Exception {
    String businessModel = bpmnPoolExtractionRoundtrip("HEMERA-2379.sgx", false, null, null, false, null);
    
    // initial x/y-coordinates of messageflow-endpoint (messageflow from non technical pool to technical pool)
    assertXpathEvaluatesTo("1840.6666666666667", "//bpmndi:BPMNEdge[@bpmnElement='messageFlow_24']/omgdi:waypoint[last()]/@x", businessModel);
    assertXpathEvaluatesTo("852.8122743336277", "//bpmndi:BPMNEdge[@bpmnElement='messageFlow_24']/omgdi:waypoint[last()]/@y", businessModel);
    
    // initial x/y-coordinates of messageflow-startpoint (messageflow from technical pool to non technical pool)
    assertXpathEvaluatesTo("1807.3333333333333", "//bpmndi:BPMNEdge[@bpmnElement='sid-3630ECEC-DC84-43F3-8373-6EE89E88DA7A']/omgdi:waypoint[1]/@x", businessModel);
    assertXpathEvaluatesTo("852.8122743336277", "//bpmndi:BPMNEdge[@bpmnElement='sid-3630ECEC-DC84-43F3-8373-6EE89E88DA7A']/omgdi:waypoint[1]/@y", businessModel);
  }

  @Test
  public void test_HEMERA_2379_withImport() throws Exception {
    String roundtripResult = bpmnPoolExtractionRoundtrip("HEMERA-2379.sgx", 
            false,
            null,
            null,
            true,
            "HEMERA-2379-import.bpmn");
    
    // x/y-coordinates of the messageflow-endpoint after the import of the changed technical model (messageflow from non technical pool to technical pool)
    assertXpathEvaluatesTo("1840.6666666666665", "//bpmndi:BPMNEdge[@bpmnElement='messageFlow_24']/omgdi:waypoint[last()]/@x", roundtripResult);
    assertXpathEvaluatesTo("1017.8122743336279", "//bpmndi:BPMNEdge[@bpmnElement='messageFlow_24']/omgdi:waypoint[last()]/@y", roundtripResult); // the message flow-target is now the pool-border
      
    // x/y-coordinates of the messageflow-startpoint after the import of the changed technical model (messageflow from technical pool to non technical pool)
    assertXpathEvaluatesTo("1807.333333333333", "//bpmndi:BPMNEdge[@bpmnElement='sid-3630ECEC-DC84-43F3-8373-6EE89E88DA7A']/omgdi:waypoint[1]/@x", roundtripResult);
    assertXpathEvaluatesTo("1017.8122743336279", "//bpmndi:BPMNEdge[@bpmnElement='sid-3630ECEC-DC84-43F3-8373-6EE89E88DA7A']/omgdi:waypoint[1]/@y", roundtripResult); // the message flow-source is now the pool-border
    
    assertXpathEvaluatesTo("AutomatedAcquisition", "//bpmn:message[1]/@name", roundtripResult);
    assertXpathEvaluatesTo("ManualAcquisition", "//bpmn:message[2]/@name", roundtripResult);
  }
  
  @Test
  public void test_HEMERA_1791() throws Exception {
    // shows also that pool is removed if it's the only participant in collaboration
    bpmnSimplePoolExtractionRoundtripWithDevFriendlyEngineDeploy("HEMERA-1791.bpmn");
  }
  
  @Test
  public void test_HEMERA_1791_Collaboration() throws Exception {
    // shows also that pool is retained if it's no the only participant in collaboration
    bpmnSimplePoolExtractionRoundtripWithDevFriendlyEngineDeploy("HEMERA-1791-Collaboration.bpmn");
  }
  
  @Test
  public void test_HEMERA_1820() throws Exception {
    bpmnSimplePoolExtractionRoundtripWithDevFriendlyEngineDeploy("HEMERA-1820.bpmn");
  }
  
  @Test
  public void test_HEMERA_1821() throws Exception {
    bpmnSimplePoolExtractionRoundtripWithDevFriendlyEngineDeploy("HEMERA-1821.bpmn");
  }
  
  @Test
  public void test_HEMERA_1942() throws Exception {
    bpmnSimplePoolExtractionRoundtripWithDevFriendlyEngineDeploy("HEMERA-1942.sgx");
  }
  
  //@Ignore(value="Disabled because of HEMERA-3125")
  @Test
  public void test_ErrorBoundaryEventsInSubProcess() throws Exception {
    bpmnPoolExtractionRoundtrip("SubprocessBoundaryEventBug.sgx", false, null, null, true, null);
  }
  
  
// -------------------------------- CONVIENCE METHODS ------------------------------ //
  
  private void bpmnSimplePoolExtractionRoundtripWithDevFriendlyEngineDeploy(String filename) throws Exception {
    bpmnPoolExtractionRoundtripWithEngineDeploy(filename, true, null, null);
  }

  private void bpmnSimplePoolExtractionRoundtripWithDevFriendlyEngineDeploy(String filename, String replaceRegex, String withReplacement) throws Exception {
    bpmnPoolExtractionRoundtripWithEngineDeploy(filename, true, replaceRegex, withReplacement);
  }

  private void bpmnPoolExtractionRoundtripWithEngineDeploy(String filename, boolean devFriendly, String replaceRegex, String withReplacement) throws Exception {
    bpmnPoolExtractionRoundtrip(filename, devFriendly, replaceRegex, withReplacement, true);
  }

  private void bpmnPoolExtractionRoundtrip(String filename, boolean devFriendly, String replaceRegex, String withReplacement, boolean deployToEngine) throws Exception {
    bpmnPoolExtractionRoundtrip(filename, devFriendly, replaceRegex, withReplacement, deployToEngine, null);
  }

  /**
   * Helper which allows to import a signavioArchive or a bpmn model into
   * Signavio and does the bpmn roundtrip.
   * 
   * @param filename
   *          the name of the file
   * @param devFriendly
   *          should the model be made developer friendly
   * @param replaceRegex
   *          replace the given string
   * @param withReplacement
   *          with this string
   * @param deployToEngine
   *          should the exported technical model deployed to engine
   */
  private String bpmnPoolExtractionRoundtrip(String filename, boolean devFriendly, String replaceRegex, String withReplacement, boolean deployToEngine,
          String importXmlFile) throws Exception {
    // enable writing of results to files
    IoUtil.DEBUG = true;
    IoUtil.DEBUG_DIR = DEBUG_DIR;

    String initialRawBpmn20Xml = null; // the initial business model
    String technicalModel = null;
    String actualRawBpmn20Xml = null; // the business model after completed roundtrip
    
    InputStream initialRawBpmn20XmlInputStream = null; // the initial business model
    InputStream technicalModelInputStream = null;
    InputStream actualRawBpmn20XmlInputStream = null; // the business model after completed roundtrip
    
    ConnectorNode folder = null;
    
    try {
      // create directory
      String folderName = "Cycle: SignavioConnectorIT.testBpmnPoolExtractionRoundtrip " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
      folder = getSignavioConnector().createNode(getSignavioConnector().getPrivateFolder().getId(), folderName, ConnectorNodeType.FOLDER, null);

      // upload model
      if (filename.endsWith(".sgx")) {
        getSignavioConnector().importSignavioArchive(folder, TEST_RESOURCES_PATH + filename);
      } else if (filename.endsWith(".bpmn")) {
        getSignavioConnector().importContent(folder, readModel(filename), filename.substring(0, filename.indexOf(".")));
      } else {
        fail("Unable to determine type of file to upload! [File=" + filename + "]");
      }

      List<ConnectorNode> models = getSignavioConnector().getChildren(folder);
      assertTrue("No models were imported!", models.size() > 0);
      for (ConnectorNode node : models) {
        assertTrue(node instanceof ConnectorNode);
        ConnectorNode model = (ConnectorNode) node;
        System.out.println("Testing Pool Extraction Roundtrip with model '" + model.getLabel() + "'...");

        
        initialRawBpmn20XmlInputStream = getSignavioConnector().getContent(model);
        initialRawBpmn20Xml = IOUtils.toString(initialRawBpmn20XmlInputStream, "UTF-8");
        IoUtil.closeSilently(initialRawBpmn20XmlInputStream);
        IoUtil.writeStringToFileIfDebug(initialRawBpmn20Xml, "initial_raw_model", INITIAL_RAW_BPMN20_XML);
        
        // export (developer-friendly) BPMN 2.0 XML of Engine Pool
        if (importXmlFile == null) {
          if (devFriendly) {
            String enginePoolId = model.getLabel().replaceFirst("^[^a-zA-Z]", "z").replaceAll("[^a-zA-Z0-9-]", "_").concat("_");
            initialRawBpmn20XmlInputStream = IOUtils.toInputStream(initialRawBpmn20Xml, "UTF-8");
            ByteArrayOutputStream devFriendlyOutput = XsltTransformer.instance().developerFriendly(initialRawBpmn20XmlInputStream, enginePoolId,  true);
            initialRawBpmn20Xml = devFriendlyOutput.toString("UTF-8");
            IoUtil.closeSilently(initialRawBpmn20XmlInputStream);
            IoUtil.closeSilently(devFriendlyOutput);
          }
          initialRawBpmn20XmlInputStream = IOUtils.toInputStream(initialRawBpmn20Xml, "UTF-8");
          technicalModelInputStream = bpmnProcessModelUtil.extractExecutablePool(initialRawBpmn20XmlInputStream);
          technicalModel = IOUtils.toString(technicalModelInputStream, "UTF-8");
          IoUtil.closeSilently(technicalModelInputStream, initialRawBpmn20XmlInputStream);
        } else {
          technicalModel = readModel(importXmlFile);
        }
        
        IoUtil.writeStringToFileIfDebug(technicalModel, "technical_model", TECHNICAL_BPMN20_XML);

        // do some changes in the model
        if (replaceRegex != null && !replaceRegex.isEmpty() && withReplacement != null) {
          technicalModel = changeTechnicalModel(replaceRegex, withReplacement, technicalModel);
          IoUtil.writeStringToFileIfDebug(technicalModel, "changed_technical_model", CHANGED_TECHNICAL_BPMN20_XML);
        }

        // test if technical model deploys to engine
        if (deployToEngine) {
          IoUtil.writeStringToFileIfDebug(technicalModel, "technical_model_before_deploy", BEFORE_DEPLOY_TECHNICAL_BPMN20_XML);
          validateActivitiDeployable(technicalModel, filename);
        }

        // import Engine Pool back into collaboration
        actualRawBpmn20Xml = bpmnProcessModelUtil.importChangesFromExecutableBpmnModel(technicalModel, initialRawBpmn20Xml);
        actualRawBpmn20XmlInputStream = IOUtils.toInputStream(actualRawBpmn20Xml, "UTF-8");
        getSignavioConnector().updateContent(model, actualRawBpmn20XmlInputStream, "update");
        IoUtil.closeSilently(actualRawBpmn20XmlInputStream);
        actualRawBpmn20XmlInputStream = getSignavioConnector().getContent(model);
        actualRawBpmn20Xml = IOUtils.toString(actualRawBpmn20XmlInputStream, "UTF-8");
        IoUtil.closeSilently(actualRawBpmn20XmlInputStream);
        
        IoUtil.writeStringToFileIfDebug(actualRawBpmn20Xml, "actual_model", ACTUAL_RAW_BPMN20_XML);

        // compare result with a previous result stored in TEST_RESOURCES_PATH
        assertRoundtripResultCorrect(filename, importXmlFile, model, replaceRegex, withReplacement, actualRawBpmn20Xml);

      }
    } finally {
      if (folder != null) {
        // delete folder
        getSignavioConnector().deleteNode(folder, null);
      }
    }

    return actualRawBpmn20Xml; // last result
  }

  private void assertRoundtripResultCorrect(String filename, String importXmlFile, ConnectorNode model, String replaceRegex, String withReplacement,
          String actualRawBpmn20Xml) {
    String changeId = DigestUtils.md5Hex(replaceRegex + withReplacement);
    String expectedRawBpmn20XmlFileName = TEST_RESOURCES_PATH + filename + "+" + importXmlFile + "_" + model.getLabel() + "_change-" + changeId
            + "_" + EXPECTED_RAW_BPMN20_XML;
    try {
      File expectedRawBpmn20XmlFile = new File(expectedRawBpmn20XmlFileName);
      if (OVERWRITE_EXPECTED_BPMN_FILES) {
        FileUtils.writeStringToFile(expectedRawBpmn20XmlFile, actualRawBpmn20Xml, "UTF-8");
        fail("The assertions of this test only work if SignavioConnectorIT#OVERWRITE_EXPECTED_BPMN_FILES is set to false.");
      }
      String expectedRawBpmn20Xml = FileUtils.readFileToString(expectedRawBpmn20XmlFile, "UTF-8").replace("\r", ""); // remove carriage returns in case the files have been fetched via Git on Windows
      IoUtil.writeStringToFileIfDebug(expectedRawBpmn20Xml, "expected_model", EXPECTED_RAW_BPMN20_XML); // just for convenient comparison
      
      DetailedDiff details = compareSignavioBpmn20Xml(expectedRawBpmn20Xml, actualRawBpmn20Xml);
      IoUtil.writeStringToFileIfDebug("Comparison:" + "\n" + details.toString(), "comparison_details", XML_DIFF_TXT);
      
      // show non-recoverable differences if the assertion fails
      assertTrue("Comparison:" + "\n" + details.toString().replaceAll("\\[not identical\\] [^\n]+\n", "").replaceAll("\n\n+", "\n"), details.similar());
    } catch (IOException e) {
      throw new RuntimeException("Unable to read or write expected result: " + expectedRawBpmn20XmlFileName, e);
    }
  }
  
  /**
   * Compares two BPMN 2.0 XML files exported by Signavio using XMLUnit.
   * 
   * Note that XMLUnit is configured in {@link SignavioConnectorIT#setUp()}.
   */
  private DetailedDiff compareSignavioBpmn20Xml(String expectedRawBpmn20Xml, String actualRawBpmn20Xml) {
    try {
      Diff diff = XMLUnit.compareXML(expectedRawBpmn20Xml, actualRawBpmn20Xml);
      DetailedDiff details = new DetailedDiff(diff);
      details.overrideDifferenceListener(new SignavioBpmn20XmlDifferenceListener());
      details.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
      return details;
    } catch (SAXException e) {
      throw new RuntimeException("Exception during XML comparison.", e);
    } catch (IOException e) {
      throw new RuntimeException("Exception during XML comparison.", e);
    }
  }

  private List<String> readModels() {
    ArrayList<String> models = new ArrayList<String>();
    for (String model : testBpmnModels) {
      models.add(readModel(model));
    }
    return models;
  }

  private String readModel(String modelName) {
    return new Scanner(SignavioConnectorIT.class.getClass().getResourceAsStream("/" + MODEL_FOLDER + "/" + modelName), "UTF-8").useDelimiter("\\A").next();
  }

  private void validateActivitiDeployable(String bpmnXml, String name) {
    // parse to validate
    // TODO: Okay, this needs more serious thinking where we get the engine
    // from!
    ExpressionManager expressionManager = processEngineConfiguration.getExpressionManager();
    BpmnParseFactory bpmnParseFactory = new DefaultBpmnParseFactory();
    BpmnParser bpmnParser = new BpmnParser(expressionManager, bpmnParseFactory);
    Context.setProcessEngineConfiguration(processEngineConfiguration);

    // Unfortunately the deployment id is requested while parsing, so we have to
    // set a DeploymentEntity to avoid an NPE
    DeploymentEntity deployment = new DeploymentEntity();
    deployment.setId("VALIDATION_DEPLOYMENT");

    // parse to validate
    BpmnParse parse = bpmnParser.createParse().deployment(deployment).sourceString(bpmnXml).name(name);
    // parse.execute();
    // That's it, now we get an exception if the file is invalid
  }

  private String changeTechnicalModel(String replaceRegex, String withReplacement, String technicalModel) {
    Pattern pattern = Pattern.compile(replaceRegex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    Matcher matcher = pattern.matcher(technicalModel);
    assertTrue("Technical model doesn't contain replace string=[" + replaceRegex + "]" + "\n" + technicalModel, matcher.find());
    technicalModel = matcher.replaceFirst(withReplacement);

    // get matched id
    if (matcher.groupCount() > 0) {
      String replaceReferencedElementId = matcher.group(1);
      System.out.println(matcher.group(1));
      // adjust all affected ids
      Pattern replaceIdPattern = Pattern.compile("id=\"(.*?)\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
      Matcher idMatcher = replaceIdPattern.matcher(withReplacement);
      if (idMatcher.find()) {
        System.out.println(idMatcher.group(1));
        technicalModel = technicalModel.replaceAll(replaceReferencedElementId, idMatcher.group(1));
      }
    }

    // if we have specified an activiti attribute in our replacement text, add
    // activiti namespace
    if (withReplacement.contains("activiti:")) {
      Pattern definitionsPattern = Pattern.compile("<definitions", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
      assertTrue("Technical model doesn't contain replace string=[" + "<definitions" + "]" + "\n" + technicalModel, definitionsPattern.matcher(technicalModel)
              .find());
      technicalModel = definitionsPattern.matcher(technicalModel).replaceFirst("<definitions xmlns:activiti=\"http://activiti.org/bpmn\"");
    }
    return technicalModel;
  }

  private SignavioConnector getSignavioConnector() {
    return signavioConnector;
  }
  
}

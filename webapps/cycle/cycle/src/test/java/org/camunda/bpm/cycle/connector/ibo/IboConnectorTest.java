package org.camunda.bpm.cycle.connector.ibo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.inject.Inject;

import org.camunda.bpm.cycle.connector.ConnectorNode;
import org.camunda.bpm.cycle.connector.ConnectorNodeType;
import org.camunda.bpm.cycle.connector.ContentInformation;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/test/ibo-connector-xml-config.xml" })
public class IboConnectorTest {

	@Inject
	private IboConnector iboConnector;

	private String documentId;

	@Before
	public void setUp() throws Exception {
		documentId = System.getProperty("test.documentId");

		assertNotNull(documentId);
		getIboConnector().init(getIboConnector().getConfiguration());
		if (getIboConnector().needsLogin()) {
			try {
			getIboConnector().login(getIboConnector().getConfiguration().getGlobalUser(),
					getIboConnector().getConfiguration().getGlobalPassword());
			} catch (Exception e) {
				fail("Something went wrong: Login failed with following exception: " + e.getMessage());
			}
		}
	}

	@After
	public void tearDown() throws Exception {
		getIboConnector().dispose();
	}

	@Test
	public void testConfigurableViaXml() throws Exception {
		assertNotNull(getIboConnector());

		ConnectorConfiguration config = getIboConnector().getConfiguration();
		assertNotNull(config);
		assertTrue(config.getId() == 2);

		assertEquals("My IboConnector", config.getName());

		Map<String, String> prop = config.getProperties();
		assertNotNull(prop);

		assertNotNull(config.getLoginMode());
		assertNotNull(config.getGlobalUser());
		assertNotNull(config.getGlobalPassword());
	}

	@Test
	public void testGetRootNode() {
		ConnectorNode node = getIboConnector().getRoot();

		assertNotNull(node);
		assertNotNull(node.getId());
		assertTrue(node.getType() == ConnectorNodeType.FOLDER);
	}

	@Test
	public void testGetChildren() {
		List<ConnectorNode> nodeList = getIboConnector().getChildren(new ConnectorNode("/", ConnectorNodeType.FOLDER));

		assertTrue(nodeList.size() > 0);
	}

	@Test
	public void testGetContentInformation() {
		ContentInformation contentInfo = getIboConnector().getContentInformation(new ConnectorNode(documentId));

		assertNotNull(contentInfo);
	}

	@Test
	public void testGetPng() {
		InputStream is = getIboConnector().getContent(new ConnectorNode(documentId, ConnectorNodeType.PNG_FILE));
		assertNotNull(is);
	}

	@Test
	public void testGetXml() {
		InputStream is = getIboConnector().getContent(new ConnectorNode(documentId, ConnectorNodeType.BPMN_FILE));
		assertNotNull(is);
	}

	@Test
	public void testUpdateDocument() {
		// import new model content
		String modelName = "HEMERA-2219";
		String newContent = new Scanner(getClass().getResourceAsStream("/models/" + modelName + "-import.bpmn"), "UTF-8")
				.useDelimiter("\\A").next();
		try {
			ContentInformation info = getIboConnector().updateContent(
					new ConnectorNode(documentId, "testlabel", ConnectorNodeType.BPMN_FILE),
					new ByteArrayInputStream(newContent.getBytes()), modelName);
			assertNotNull(info);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testCreateFolder() {
		ConnectorNode node = getIboConnector().createNode("/", "mylabel", ConnectorNodeType.FOLDER, "Create Folder");
		assertNotNull(node);
		assertNotNull(node.getId());
		assertTrue(node.getType() == ConnectorNodeType.FOLDER);
	}

	@Test
	public void testCreateFile() {
		ConnectorNode node = getIboConnector().createNode("/", "mylabel", ConnectorNodeType.BPMN_FILE, "Create File");
		assertNotNull(node);
		assertNotNull(node.getId());
		assertTrue(node.getType() == ConnectorNodeType.BPMN_FILE);
	}

	@Test
	public void testDeleteFolder() {
		getIboConnector().deleteNode(new ConnectorNode(documentId, ConnectorNodeType.FOLDER), "Delete Folder");
	}

	@Test
	public void testDeleteFile() {
		getIboConnector().deleteNode(new ConnectorNode(documentId, ConnectorNodeType.BPMN_FILE), "Delete File");
	}

	private IboConnector getIboConnector() {
		return iboConnector;
	}
}
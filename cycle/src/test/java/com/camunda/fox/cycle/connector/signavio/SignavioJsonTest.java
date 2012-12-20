package com.camunda.fox.cycle.connector.signavio;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import com.camunda.fox.cycle.util.IoUtil;


public class SignavioJsonTest {

  private static String json = null;
          
  
  @BeforeClass
  public static void init() {
    json = IoUtil.readFileAsString("com/camunda/fox/cycle/connector/signavio/getChildrenRootNodeResponse.json");
  }
  
  @Test
  public void testExtractPrivateFolderId() {
    String privateFolderId = SignavioJson.extractPrivateFolderId(json);
    assertThat(privateFolderId).isEqualTo("/baca2d6ad18043ebb02e730124f64b66");
  }
  
  @Test
  public void testExtractPrivateFolderName() {
    String privateFolderId = SignavioJson.extractPrivateFolderName(json);
    assertThat(privateFolderId).isEqualTo("Meine Dokumente");
  }
}

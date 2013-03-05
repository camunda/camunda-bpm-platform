package com.camunda.fox.cycle.connector.signavio;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.camunda.fox.cycle.connector.ConnectorNodeType;

/**
 * Handles all JSON-related stuff, like extraction of ids, types, conversion
 * etc.
 * 
 * @author christian.lipphardt@camunda.com
 */
public class SignavioJson {

  // JSON properties/objects
  private static final String JSON_REP_OBJ = "rep";
  private static final String JSON_REL_PROP = "rel";
  private static final String JSON_HREF_PROP = "href";
  private static final String JSON_NAME_PROP = "name";
  private static final String JSON_TITLE_PROP = "title";
  private static final String JSON_PARENT_PROP = "parent";
  private static final String JSON_PARENT_NAME_PROP = "parentName";
  private static final String JSON_TYPE_PROP = "type";
  private static final String JSON_NAMESPACE_PROP = "namespace";
  private static final String JSON_UPDATED_PROP = "updated";
  private static final String JSON_COMMENT_PROP = "comment";

  // JSON values
  private static final String JSON_DIR_VALUE = "dir";
  private static final String JSON_MOD_VALUE = "mod";
  private static final String JSON_PRIVATE_VALUE = "private";
  private static final String JSON_TYPE_BPMN20_VALUE = "BPMN 2.0";
  private static final String JSON_NAMESPACE_BPMN20_VALUE = "bpmn2.0";

  public static String extractNodeName(String json) {
    try {
      return extractNodeName(new JSONObject(json));
    } catch (JSONException e) {
      throw new RuntimeException("Unable to extract node name.", e);
    }
  }
  
  public static String extractNodeName(JSONObject jsonObj) {
    try {
      String label = "";
      JSONObject repJsonObj = jsonObj.getJSONObject(JSON_REP_OBJ);
      if (repJsonObj.has(JSON_NAME_PROP)) {
        label = repJsonObj.getString(JSON_NAME_PROP);
      } else if (repJsonObj.has(JSON_TITLE_PROP)) {
        label = repJsonObj.getString(JSON_TITLE_PROP);
      }
      return label;
    } catch (JSONException e) {
      throw new RuntimeException("Unable to extract node name.", e);
    }
  }

  public static String extractDirectoryId(JSONObject jsonObj) {
    try {
      String directoryId = jsonObj.getString(JSON_HREF_PROP);
      directoryId = directoryId.replace(SignavioClient.SLASH_CHAR + SignavioClient.DIRECTORY_URL_SUFFIX, "");
      return directoryId;
    } catch (JSONException e) {
      throw new RuntimeException("Unable to extract directory id.", e);
    }
  }

  public static String extractModelId(JSONObject jsonObj) {
    try {
      String directoryId = jsonObj.getString(JSON_HREF_PROP);
      directoryId = directoryId.replace(SignavioClient.SLASH_CHAR + SignavioClient.MODEL_URL_SUFFIX, "");
      return directoryId;
    } catch (JSONException e) {
      throw new RuntimeException("Unable to extract model id.", e);
    }
  }

  public static ConnectorNodeType extractModelContentType(JSONObject jsonObj) {
    try {
      JSONObject repJsonObj = jsonObj.getJSONObject(JSON_REP_OBJ);
      
      if ((repJsonObj.has(JSON_TYPE_PROP) && JSON_TYPE_BPMN20_VALUE.equals(repJsonObj.getString(JSON_TYPE_PROP))) ||
          (repJsonObj.has(JSON_NAMESPACE_PROP) && repJsonObj.getString(JSON_NAMESPACE_PROP).contains(JSON_NAMESPACE_BPMN20_VALUE))) {
        return ConnectorNodeType.BPMN_FILE;
      } else {
        return ConnectorNodeType.ANY_FILE;
      }
    } catch (JSONException e) {
      throw new RuntimeException("Unable to extract content type.", e);
    }
  }
  
  public static String extractPrivateFolderId(String json) {
    try {
      JSONArray jsonArray = new JSONArray(json);
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject jsonObj = jsonArray.getJSONObject(i);
        String rel = jsonObj.getString(JSON_REL_PROP);
        if (rel.equals(JSON_DIR_VALUE)) {
          JSONObject repJsonObj = jsonObj.getJSONObject(JSON_REP_OBJ);
          String type = repJsonObj.getString(JSON_TYPE_PROP);
          if (type.equals(JSON_PRIVATE_VALUE)) {
            String directoryId = jsonObj.getString(JSON_HREF_PROP);
            directoryId = directoryId.replace(SignavioClient.SLASH_CHAR + SignavioClient.DIRECTORY_URL_SUFFIX, "");
            return directoryId; 
          }
        }
      }
      throw new RuntimeException("Unable to determine private folder id");
    } catch (Exception e) {
      throw new RuntimeException("Unable to determine private folder id", e);
    }
  }
  
  public static String extractPrivateFolderName(String json) {
    try {
      JSONArray jsonArray = new JSONArray(json);
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject jsonObj = jsonArray.getJSONObject(i);
        String rel = jsonObj.getString(JSON_REL_PROP);
        if (rel.equals(JSON_DIR_VALUE)) {
          JSONObject repJsonObj = jsonObj.getJSONObject(JSON_REP_OBJ);
          String type = repJsonObj.getString(JSON_TYPE_PROP);
          if (type.equals(JSON_PRIVATE_VALUE)) {
            return extractNodeName(jsonObj);
          }
        }
      }
      throw new RuntimeException("Unable to determine private folder name");
    } catch (Exception e) {
      throw new RuntimeException("Unable to determine private folder name", e);
    }
  }

  public static String extractLastModifiedDateFromInfo(String info) {
    try {
      JSONObject jsonObj = new JSONObject(info);
      return jsonObj.getString(JSON_UPDATED_PROP);
    } catch (JSONException e) {
      throw new RuntimeException("Unable to extract last modified date.", e);
    }
  }
  
  public static String extractParentIdFromInfo(String info) {
    try {
      JSONObject jsonObj = new JSONObject(info);
      return jsonObj.getString(JSON_PARENT_PROP).replace(SignavioClient.SLASH_CHAR + SignavioClient.DIRECTORY_URL_SUFFIX, "");
    } catch (JSONException e) {
      throw new RuntimeException("Unable to extract parent id.", e);
    }
  }
  
  public static String extractParentNameFromInfo(String info) {
    try {
      JSONObject jsonObj = new JSONObject(info);
      return jsonObj.getString(JSON_PARENT_NAME_PROP);
    } catch (JSONException e) {
      throw new RuntimeException("Unable to extract parent name.", e);
    }
  }
  
  public static String extractIdForMatchingModelName(String json, String modelName) throws JSONException {
    JSONArray jsonArray = new JSONArray(json);
    for (int i = 0; i < jsonArray.length(); i++) {
      JSONObject jsonObj = jsonArray.getJSONObject(i);

      String relProp = jsonObj.getString(JSON_REL_PROP);
      if (relProp.equals(JSON_DIR_VALUE) && SignavioJson.extractNodeName(jsonObj).equals(modelName)) {
        return SignavioJson.extractDirectoryId(jsonObj);
      } else if (relProp.equals(JSON_MOD_VALUE) && SignavioJson.extractNodeName(jsonObj).equals(modelName)) {
        return SignavioJson.extractModelId(jsonObj);
      }
    }
    
    throw new RuntimeException("Unable to get id for model named: " + modelName);
  }

  public static String extractModelComment(JSONObject jsonObj) {    
    try {
      String comment = "";
      JSONObject repJsonObj = jsonObj.getJSONObject(JSON_REP_OBJ);
      if (repJsonObj.has(JSON_COMMENT_PROP)) {
        comment = repJsonObj.getString(JSON_COMMENT_PROP);
      }
      return comment;
    } catch (JSONException e) {
      throw new RuntimeException("Unable to extract comment.", e);
    }
  }
    
}

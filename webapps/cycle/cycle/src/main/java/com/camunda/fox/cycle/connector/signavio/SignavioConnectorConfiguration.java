package com.camunda.fox.cycle.connector.signavio;



public class SignavioConnectorConfiguration {

  // these values differ between Oryx and Signavio
  protected static String REPOSITORY_BACKEND_URL_SUFFIX = "p/";
  protected static String EDITOR_BACKEND_URL_SUFFIX = "editor/";
  protected static String EDITOR_URL_SUFFIX = "editor?id=";

  public static String REGISTRATION_URL_SUFFIX = "register/";
  public static String LOGIN_URL_SUFFIX = "login/";
  public static String EXPLORER_URL_SUFFIX = "explorer/";
  public static String EXPLORER_DIRECTORY_URL_SUFFIX = "explorer#/directory/";
  public static String PORTAL_URL_SUFFIX = "portal#/model/";
  public static String MODEL_URL_SUFFIX = "model";
  public static String DIRECTORY_URL_SUFFIX = "directory";
  public static String MASHUP_URL_SUFFIX = "mashup/";
  public static String MODEL_INFO_URL_SUFFIX = "info";
  public static String STENCILSETS_URL_SUFFIX = "stencilsets/stencilsets.json";

  public static String BPMN_20_EXPORT_SERVLET = "bpmn2_0serialization";
  // public static String BPMN_20_IMPORT_SERVLET = "bpmn2_0deserialization";
  public static String BPMN_20_IMPORT_SERVLET = "bpmn2_0-import";
  public static String SIGNAVIO_ARCHIVE_IMPORT_SERVLET = "zip-import";

  protected SignavioConnector connector;

//  public SignavioConnectorConfiguration(SignavioConnector connector) {
//    this.connector = connector;
//  }
//
//  public String getSignavioUrl() {
//    return connector.getSignavioUrl();
//  }
//
//  public String getRepositoryBackendUrl() {
//    return connector.getSignavioUrl() + getRepositoryBackendUrlSuffix();
//  }
//
//  public String getEditorBackendUrl() {
//    return connector.getSignavioUrl() + getEditorBackendUrlSuffix();
//  }
//
//  public String getDirectoryIdFromInfoUrl(String href) {
//    String url = getDirectoryIdFromUrl(href);
//    // to remove '/info' from url end
//    url = url.substring(0, url.length() - 5);
//    return url;
//  }
//
//  public String getDirectoryIdFromUrl(String href) {
//    return retrieveIdFromUrl(href, "/" + DIRECTORY_URL_SUFFIX);
//  }
//
//  public String getModelIdFromUrl(String href) {
//    return retrieveIdFromUrl(href, "/" + MODEL_URL_SUFFIX);
//  }
//
//  /**
//   * get the part of the URL identifying the real ID needed to be stored in the
//   * API object to be able to identify the object later on
//   */
//  private String retrieveIdFromUrl(String href, String baseUrl) {
//    // TODO: Check implementation!
//    return href.replaceAll(baseUrl, "");
//  }
//
//  public String getModelUrl(String id) {
//    if (id.startsWith("/")) {
//      // don't encode this one!
//      id = id.substring(1);
//    }
//    return getRepositoryBackendUrl() + MODEL_URL_SUFFIX + "/" + encode(id);
//  }
//
//  private String encode(String id) {
//    try {
//      return URLEncoder.encode(id, "UTF-8");
//    } catch (UnsupportedEncodingException ex) {
//      throw new RepositoryException("Couldn't UTF-8 encode id '" + id + "' for Signavio.", ex);
//    }
//  }
//
//  public String getDirectoryUrl(String id) {
//    if (id.startsWith("/")) {
//      // don't encode this one!
//      id = id.substring(1);
//    }
//    return getRepositoryBackendUrl() + DIRECTORY_URL_SUFFIX + encode(id);
//  }
//
//  public String getRegistrationUrl() {
//    return getRepositoryBackendUrl() + REGISTRATION_URL_SUFFIX;
//  }
//
//  public String getLoginUrl() {
//    return getRepositoryBackendUrl() + LOGIN_URL_SUFFIX;
//  }
//
//  public String getEditorUrl(String id) {
//    String url = getRepositoryBackendUrl() + EDITOR_URL_SUFFIX;
//    return appendIdToUrl(url, id);
//  }
//
//  public String getPortalUrl(String id) {
//    String url = getRepositoryBackendUrl() + PORTAL_URL_SUFFIX;
//    return appendIdToUrl(url, id);
//  }
//  
//  public String getExplorerUrlForDirectory(String id) {
//    String url = getRepositoryBackendUrl() + EXPLORER_DIRECTORY_URL_SUFFIX;
//    return appendIdToUrl(url, id);
//  }
//  
//  private String appendIdToUrl(String url, String id) {
//    if (id.startsWith("/")) {
//      // this is how it should be now
//      return url + id.substring(1);
//    } else {
//      // this is how it was in ancient times
//      return url + id;
//    }
//  }
//
//  public String getPngUrl(String id, String securityToken) {
//    return getModelUrl(id) + "/png?"
//    // old: "token=" + securityToken
//    // old token MUST be removed from URL, otherwise it doesn't work on SaaS
//    // new
//     + "signavio-id=" + securityToken;
//  }
//
//  public String getExplorerUrl() {
//    return getRepositoryBackendUrl() + EXPLORER_URL_SUFFIX;
//  }
//
//  /**
//   * TODO: Rename?
//   */
//  public String getModelRootUrl() {
//    return getRepositoryBackendUrl() + MODEL_URL_SUFFIX + "/";
//  }
//
//  public String getDirectoryRootUrl() {
//    return getRepositoryBackendUrl() + DIRECTORY_URL_SUFFIX + "/";
//  }
//
//  public String getMashupUrl() {
//    return getRepositoryBackendUrl() + MASHUP_URL_SUFFIX;
//  }
//
//  public String getBpmn20XmlExportServletUrl() {
//    return getEditorBackendUrl() + BPMN_20_EXPORT_SERVLET;
//  }
//
//  public String getBpmn20XmlImportServletUrl() {
//    return getSignavioUrl() + REPOSITORY_BACKEND_URL_SUFFIX + BPMN_20_IMPORT_SERVLET;
//  }
//
//  /**
//   * TODO: What is that and why it is needed?
//   */
//  public String getPurl() {
//    return getRepositoryBackendUrl() + "purl";
//  }
//
//  public String getModelInfoUrl(String modelId) {
//    return getModelUrl(modelId) + "/" + MODEL_INFO_URL_SUFFIX;
//  }
//
//  public String getRepositoryBackendUrlSuffix() {
//    return REPOSITORY_BACKEND_URL_SUFFIX;
//  }
//
//  public String getEditorBackendUrlSuffix() {
//    return EDITOR_BACKEND_URL_SUFFIX;
//  }
//
//  public String getStencilsetsUrl() {
//    return getEditorBackendUrl() + STENCILSETS_URL_SUFFIX;
//  }
//
//  public String getSignavioArchiveImportServletUrl() {
//    return getSignavioUrl() + REPOSITORY_BACKEND_URL_SUFFIX + SIGNAVIO_ARCHIVE_IMPORT_SERVLET;
//  }
//  
}

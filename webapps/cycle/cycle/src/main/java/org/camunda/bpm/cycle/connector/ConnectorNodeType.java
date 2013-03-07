package org.camunda.bpm.cycle.connector;

/**
 * Type of a node returned by a connector.
 * 
 * This abstraction is required to capture connector internal information 
 * about a file type which may not be derivable from e.g. a file name.
 * 
 * (One case is Signavio, where the connector knows about a models file type, only).
 * 
 * @author nico.rehwaldt
 */
public enum ConnectorNodeType {
  
  /** 
   * Special case which is not yet specified 
   */
  UNSPECIFIED(null, false), 
  
  FOLDER(null, false), 
  ANY_FILE("text/plain", true), 
  BPMN_FILE("application/xml", true), 
  PNG_FILE("image/png", true);
  
  /**
   * The mime type of the file, when serving it to a client
   */
  private String mimeType;
  
  /**
   * Is this file type representing a file?
   */
  private boolean file;

  private ConnectorNodeType(String mimeType, boolean file) {
    this.mimeType = mimeType;
    this.file = file;
  }

  public String getMimeType() {
    return mimeType;
  }

  public boolean isFile() {
    return file;
  }

  public boolean isAnyOf(ConnectorNodeType... types) {
    for (ConnectorNodeType t : types) {
      if (t == this) {
        return true;
      }
    }
    return false;
  }

  public boolean isDirectory() {
    return !isFile();
  }
  
}

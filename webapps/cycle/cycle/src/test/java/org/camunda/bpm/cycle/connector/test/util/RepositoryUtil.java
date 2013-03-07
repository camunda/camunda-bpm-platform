package org.camunda.bpm.cycle.connector.test.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

/**
 *
 * @author nico.rehwaldt
 */
public class RepositoryUtil {

  public static String createVFSRepository(File file) throws IOException {
    clean(file);
    return file.getAbsolutePath();
  }
  
  public static void clean(File directory) throws IOException {
    if (directory.exists()) {
      if (directory.isDirectory()) {
        FileUtils.deleteDirectory(directory);
      } else {
        throw new IllegalArgumentException("Not a directory: " + directory);
      }
    }
    if (!directory.mkdirs()) {
      throw new IllegalArgumentException("Could not clean: " + directory);
    }
    
  }
  
  public static String createSVNRepository(File directory) throws IOException, SVNException {
    clean(directory);
    SVNURL url = SVNRepositoryFactory.createLocalRepository(directory, true , false);
    return url.toString();
  }
}

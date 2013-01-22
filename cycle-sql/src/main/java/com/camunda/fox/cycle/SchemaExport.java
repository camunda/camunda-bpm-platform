package com.camunda.fox.cycle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;

public class SchemaExport {

  private static final Map<String, String> DB_NAMES_TO_DIALECTS;
  
  static {
    Map<String, String> map = new HashMap<String, String>();
    map.put("db2", "org.hibernate.dialect.DB2Dialect");
    map.put("h2", "org.hibernate.dialect.H2Dialect");
    map.put("mysql", "org.hibernate.dialect.MySQL5InnoDBDialect");
    map.put("oracle", "org.hibernate.dialect.Oracle10gDialect");
    map.put("postgres", "org.hibernate.dialect.PostgreSQLDialect");
    map.put("mssql", "org.hibernate.dialect.SQLServer2008Dialect");

    DB_NAMES_TO_DIALECTS = Collections.unmodifiableMap(map);
  }
  
  private static final String FOLDER_PATTERN = "@folder@";
  private static final String DBNAME_PATTERN = "@dbname@";
  private static final String SQLMODE_PATTERN = "@sqlmode@";
  private static final String SQLMODE_CREATE = "create";
  private static final String SQLMODE_DROP = "drop";

  public static void main(String[] args) {
    boolean drop = false;
    boolean create = false;
    String outFile = null;
    String delimiter = "";
    String unitName = null;

    for (int i = 0; i < args.length; i++) {
      if (args[i].startsWith("--")) {
        if (args[i].equals("--drop")) {
          drop = true;
        } else if (args[i].equals("--create")) {
          create = true;
        } else if (args[i].startsWith("--output=")) {
          outFile = args[i].substring(9);
        } else if (args[i].startsWith("--delimiter=")) {
          delimiter = args[i].substring(12);
        }
      } else {
        unitName = args[i];
      }
    }

    Formatter formatter = FormatStyle.DDL.getFormatter();

    for (Entry<String, String> dialectMapping : DB_NAMES_TO_DIALECTS.entrySet()) {
      Ejb3Configuration jpaConfiguration = new Ejb3Configuration().configure(unitName, null);
      Configuration hibernateConfiguration = jpaConfiguration.getHibernateConfiguration();
      
      Properties hibernateProperties = new Properties();
      hibernateProperties.put(Environment.DIALECT, dialectMapping.getValue());

      Dialect dbDialect = Dialect.getDialect(hibernateProperties);
      if (create) {
        String[] createSQL = hibernateConfiguration.generateSchemaCreationScript(dbDialect);
        export(formatOutputFile(outFile, dialectMapping.getKey(), SQLMODE_CREATE), delimiter, formatter, createSQL);
      }
      if (drop) {
        String[] dropSQL = hibernateConfiguration.generateDropSchemaScript(dbDialect);
        export(formatOutputFile(outFile, dialectMapping.getKey(), SQLMODE_DROP), delimiter, formatter, dropSQL);
      }
    }
  }

  private static File formatOutputFile(String outputFile, String dbName, String sqlmode) {
    String fileName = outputFile;
    fileName = fileName.replace(FOLDER_PATTERN, sqlmode);
    fileName = fileName.replace(DBNAME_PATTERN, dbName);
    fileName = fileName.replace(SQLMODE_PATTERN, sqlmode);
    
    File file = new File(fileName);
    // create missing directories
    file.getParentFile().mkdirs();
    
    return file;
  }

  private static void export(File outFile, String delimiter, Formatter formatter, String[] createSQL) {
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(outFile);
      for (String string : createSQL) {
        writer.print(formatter.format(string) + "\n" + delimiter + "\n");
      }
    } catch (FileNotFoundException e) {
      System.err.println(e);
    } finally {
      if (writer != null)
        writer.close();
    }
  }

}

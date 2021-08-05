/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.sql.test;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.camunda.commons.utils.IoUtil;
import org.junit.Before;
import org.junit.Test;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.change.core.SQLFileChange;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.ObjectChangeFilter;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.UniqueConstraint;

public class SqlScriptTest {

  /*
   * The following unique constraints are present on both databases (manual and
   * by changelog) but are not reliably contained in database snapshots created
   * by Liquibase#diff. They have been manually confirmed and can be ignored in
   * the comparison in case they are missing in either database.
   */
  protected static final List<String> IGNORED_CONSTRAINTS = Arrays.asList(
      "ACT_UNIQ_VARIABLE",
      "CONSTRAINT_8D1",
      "ACT_UNIQ_TENANT_MEMB_USER",
      "ACT_UNIQ_TENANT_MEMB_GROUP");

  protected String projectVersion;

  @Before
  public void setup() throws Exception {
    InputStream is = getClass().getClassLoader().getResourceAsStream("properties-from-pom.properties");
    Properties properties = new Properties();
    properties.load(is);

    projectVersion = properties.getProperty("project.version");
  }

  @Test
  public void shouldEqualLiquibaseChangelogAndCreateScripts() throws Exception {
    // given
    Database manualScriptsDatabase = getDatabase("manual");
    Database liquibaseChangelogDatabase = getDatabase("liquibase");

    executeSqlScript("create", "engine", manualScriptsDatabase);
    executeSqlScript("create", "identity", manualScriptsDatabase);

    try (Liquibase liquibase = new Liquibase("changelog.xml", getAccessorForChangelogDirectory(), liquibaseChangelogDatabase)) {
      liquibase.update(new Contexts());
      DiffResult diffResult =  liquibase.diff(manualScriptsDatabase, liquibaseChangelogDatabase, new CompareControl());

      // when
      List<ChangeSet> changeSetsToApply = new DiffToChangeLog(diffResult, new CustomDiffOutputControl()).generateChangeSets();

      // then
      assertThat(changeSetsToApply).isEmpty();
    }
  }

  protected Database getDatabase(String databaseName) throws DatabaseException {
    String databaseUrl = "jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=1000;MVCC=true;LOCK_TIMEOUT=10000;MV_STORE=false";
    String databaseUser = "sa";
    String databasePassword = "";
    String databaseClass = "org.h2.Driver";
    return DatabaseFactory.getInstance().openDatabase(databaseUrl, databaseUser, databasePassword, databaseClass,
        null, null, null, new ClassLoaderResourceAccessor());
  }

  protected void executeSqlScript(String sqlFolder, String sqlScript, Database database) throws LiquibaseException {
    String statements = IoUtil.inputStreamAsString(getClass().getClassLoader().getResourceAsStream(
        String.format("sql/%s/%s_%s_%s.sql", sqlFolder, "h2", sqlScript, projectVersion)));
    SQLFileChange sqlFileChange = new SQLFileChange();
    sqlFileChange.setSql(statements);
    database.execute(sqlFileChange.generateStatements(database), null);
  }

  protected FileSystemResourceAccessor getAccessorForChangelogDirectory() throws URISyntaxException {
    return new FileSystemResourceAccessor(Paths.get(getClass().getClassLoader().getResource("sql/liquibase").toURI()).toAbsolutePath().toFile());
  }

  private static class CustomDiffOutputControl extends DiffOutputControl {

    public CustomDiffOutputControl() {
      setObjectChangeFilter(new IgnoreUniqueConstraintsChangeFilter());
    }

    private static class IgnoreUniqueConstraintsChangeFilter implements ObjectChangeFilter {

      @Override
      public boolean includeUnexpected(DatabaseObject object, Database referenceDatabase, Database comparisionDatabase) {
        return include(object);
      }

      @Override
      public boolean includeMissing(DatabaseObject object, Database referenceDatabase, Database comparisionDatabase) {
        return include(object);
      }

      @Override
      public boolean includeChanged(DatabaseObject object, ObjectDifferences differences, Database referenceDatabase, Database comparisionDatabase) {
        return include(object);
      }

      @Override
      public boolean include(DatabaseObject object) {
        if (object instanceof UniqueConstraint && IGNORED_CONSTRAINTS.contains(object.getName())) {
          return false;
        }
        return true;
      }
    }
  }
}

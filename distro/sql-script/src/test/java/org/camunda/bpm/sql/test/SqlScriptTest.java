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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.change.Change;
import liquibase.change.core.SQLFileChange;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.diff.DiffGeneratorFactory;
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
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.UniqueConstraint;
import org.camunda.commons.utils.IoUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SqlScriptTest {

  /*
   * The following unique constraints are present on both databases (manual and
   * by changelog) but are not reliably contained in database snapshots created
   * by Liquibase#diff. They have been manually confirmed and can be ignored in
   * the comparison in case they are missing in either database.
   */
  protected static final List<String> IGNORED_CONSTRAINTS = Arrays.asList(
      "ACT_UNIQ_VARIABLE",
      "CONSTRAINT_8D1",// used on all but PostgreSQL
      "ACT_HI_PROCINST_PROC_INST_ID__KEY",// used on PostgreSQL
      "ACT_UNIQ_TENANT_MEMB_USER",
      "ACT_UNIQ_TENANT_MEMB_GROUP");

  protected Properties properties;
  protected Database database;
  protected String databaseType;
  protected String projectVersion;
  protected Liquibase liquibase;
  protected DiffGeneratorFactory databaseDiffer;

  @Before
  public void setup() throws Exception {
    InputStream is = getClass().getClassLoader().getResourceAsStream("properties-from-pom.properties");
    properties = new Properties();
    properties.load(is);

    databaseType = properties.getProperty("database.type");
    projectVersion = properties.getProperty("project.version");

    database = getDatabase();
    liquibase = getLiquibase();
    databaseDiffer = DiffGeneratorFactory.getInstance();
    cleanUpDatabaseTables();
  }

  @After
  public void tearDown() throws Exception {
    cleanUpDatabaseTables();
    liquibase.close();
  }

  @Test
  public void shouldEqualLiquibaseChangelogAndCreateScripts() throws Exception {
    // given
    executeSqlScript("create", "engine");
    executeSqlScript("create", "identity");
    DatabaseSnapshot snapshotScripts = createCurrentDatabaseSnapshot();
    cleanUpDatabaseTables();

    // when set up with Liquibase changelog
    liquibase.update(new Contexts());

    // then
    DatabaseSnapshot snapshotLiquibase = createCurrentDatabaseSnapshot();
    DiffResult diffResult = databaseDiffer.compare(snapshotScripts, snapshotLiquibase, new CompareControl());
    List<ChangeSet> changeSetsToApply = new DiffToChangeLog(diffResult, new CustomDiffOutputControl())
        .generateChangeSets();

    assertThat(changeSetsToApply)
        .withFailMessage("Liquibase database schema misses changes: %s", getChanges(changeSetsToApply))
        .isEmpty();
  }

  @Test
  public void shouldEqualOldUpgradedAndNewCreatedViaLiquibase() throws Exception {
    try (Liquibase liquibaseOld = getLiquibase("scripts-old/", getDatabase())) {
      // given
      liquibase.update(new Contexts());
      DatabaseSnapshot snapshotCurrent = createCurrentDatabaseSnapshot();
      cleanUpDatabaseTables();

      // old changelog executed
      liquibaseOld.update(new Contexts());

      // when new changelog executed afterward
      liquibase.update(new Contexts());

      // then
      DatabaseSnapshot snapshotUpgraded = createCurrentDatabaseSnapshot();
      DiffResult diffResult = databaseDiffer.compare(snapshotCurrent, snapshotUpgraded, new CompareControl());
      List<ChangeSet> changeSetsToApply = new DiffToChangeLog(diffResult, new DiffOutputControl()).generateChangeSets();

      assertThat(changeSetsToApply)
          .withFailMessage("Resulting upgraded database misses changes: %s", getChanges(changeSetsToApply))
          .isEmpty();
    }
  }

  @Test
  public void shouldEqualOldUpgradedAndNewCreatedViaScripts() throws Exception {
    // given
    String currentMajorMinor = properties.getProperty("current.majorminor");
    String oldMajorMinor = properties.getProperty("old.majorminor");

    executeSqlScript("create", "engine");
    executeSqlScript("create", "identity");
    DatabaseSnapshot snapshotCurrent = createCurrentDatabaseSnapshot();

    cleanUpDatabaseTables();

    // old CREATE scripts executed
    executeSqlScript("scripts-old/", "create", "engine_" + oldMajorMinor + ".0");
    executeSqlScript("scripts-old/", "create", "identity_" + oldMajorMinor + ".0");

    // when UPGRADE scripts executed
    executeSqlScript("local-upgrade-test/", "upgrade", "engine_" + oldMajorMinor + "_patch");
    executeSqlScript("local-upgrade-test/", "upgrade", "engine_" + oldMajorMinor + "_to_" + currentMajorMinor);
    executeSqlScript("local-upgrade-test/", "upgrade", "engine_" + currentMajorMinor + "_patch");

    // then
    DatabaseSnapshot snapshotUpgraded = createCurrentDatabaseSnapshot();
    DiffResult diffResult = databaseDiffer.compare(snapshotCurrent, snapshotUpgraded, new CompareControl());
    List<ChangeSet> changeSetsToApply = new DiffToChangeLog(diffResult, new CustomDiffOutputControl())
        .generateChangeSets();

    assertThat(changeSetsToApply)
        .withFailMessage("Resulting upgraded database schema differs: %s", getChanges(changeSetsToApply))
        .isEmpty();
  }

  protected void executeSqlScript(String sqlFolder, String sqlScript) throws LiquibaseException {
    executeSqlScript("", sqlFolder, sqlScript + "_" + projectVersion);
  }

  protected void executeSqlScript(String baseDirectory, String sqlFolder, String sqlScript) throws LiquibaseException {
    String scriptFileName = String.format("%ssql/%s/%s_%s.sql", baseDirectory, sqlFolder, databaseType, sqlScript);
    String statements = IoUtil.inputStreamAsString(getClass().getClassLoader().getResourceAsStream(scriptFileName));
    SQLFileChange sqlFileChange = new SQLFileChange();
    sqlFileChange.setSql(statements);
    database.execute(sqlFileChange.generateStatements(database), null);
  }

  protected void cleanUpDatabaseTables() {
    try {
      liquibase.dropAll();
      // dropAll can be incomplete if it takes too long, second attempt should
      // clean up leftovers
      liquibase.dropAll();
    } catch (Exception e) {
      // ignored
    }
  }

  protected Database getDatabase() throws DatabaseException {
    String databaseUrl = properties.getProperty("database.url");
    String databaseUser = properties.getProperty("database.username");
    String databasePassword = properties.getProperty("database.password");
    String databaseClass = properties.getProperty("database.driver");
    return DatabaseFactory.getInstance().openDatabase(databaseUrl, databaseUser, databasePassword, databaseClass, null,
        null, null, new ClassLoaderResourceAccessor());
  }

  protected Liquibase getLiquibase() throws URISyntaxException {
    return getLiquibase("", database);
  }

  protected static Liquibase getLiquibase(String baseDirectory, Database database) throws URISyntaxException {
    return new Liquibase("camunda-changelog.xml", getAccessorForChangelogDirectory(baseDirectory), database);
  }

  protected static FileSystemResourceAccessor getAccessorForChangelogDirectory(String baseDirectory) throws URISyntaxException {
    URI changelogUri = SqlScriptTest.class.getClassLoader().getResource(baseDirectory + "sql/liquibase").toURI();
    return new FileSystemResourceAccessor(Paths.get(changelogUri).toAbsolutePath().toFile());
  }

  protected DatabaseSnapshot createCurrentDatabaseSnapshot() throws Exception {
    return SnapshotGeneratorFactory.getInstance()
        .createSnapshot(database.getDefaultSchema(), database, new SnapshotControl(database));
  }

  protected static List<String> getChanges(List<ChangeSet> changeSetsToApply) {
    return changeSetsToApply.stream()
        .flatMap(cs -> cs.getChanges().stream())
        .map(Change::getDescription)
        .collect(Collectors.toList());
  }

  protected static class CustomDiffOutputControl extends DiffOutputControl {

    public CustomDiffOutputControl() {
      setObjectChangeFilter(new IgnoreUniqueConstraintsChangeFilter());
    }

    private static class IgnoreUniqueConstraintsChangeFilter implements ObjectChangeFilter {

      @Override
      public boolean includeUnexpected(DatabaseObject object, Database referenceDatabase,
          Database comparisionDatabase) {
        return include(object);
      }

      @Override
      public boolean includeMissing(DatabaseObject object, Database referenceDatabase, Database comparisionDatabase) {
        return include(object);
      }

      @Override
      public boolean includeChanged(DatabaseObject object,
          ObjectDifferences differences,
          Database referenceDatabase,
          Database comparisionDatabase) {
        return include(object);
      }

      @Override
      public boolean include(DatabaseObject object) {
        if (object instanceof UniqueConstraint && IGNORED_CONSTRAINTS.contains(object.getName().toUpperCase())) {
          return false;
        }
        return true;
      }
    }
  }
}

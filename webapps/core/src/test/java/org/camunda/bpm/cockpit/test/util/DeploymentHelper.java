/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.cockpit.test.util;

import java.io.File;

import org.camunda.bpm.admin.Admin;
import org.camunda.bpm.admin.plugin.spi.AdminPlugin;
import org.camunda.bpm.admin.test.sample.simple.SimpleAdminPlugin;
import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.core.test.util.TestContainer;
import org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin;
import org.camunda.bpm.cockpit.plugin.test.application.TestProcessEngineProvider;
import org.camunda.bpm.cockpit.test.sample.TestProcessApplication;
import org.camunda.bpm.cockpit.test.sample.plugin.simple.SimpleCockpitPlugin;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;
import org.camunda.bpm.tasklist.Tasklist;
import org.camunda.bpm.tasklist.plugin.spi.TasklistPlugin;
import org.camunda.bpm.tasklist.test.sample.simple.SimpleTasklistPlugin;
import org.camunda.bpm.webapp.AppRuntimeDelegate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;

/**
 *
 * @author nico.rehwaldt
 */
public class DeploymentHelper {

  /**
   * Resolver to resolve external maven dependencies
   *
   * @return
   */
  public static MavenDependencyResolver resolver() {

    return DependencyResolvers
        .use(MavenDependencyResolver.class)
        .goOffline()
        .loadMetadataFromPom("pom.xml");
  }

  /**
   * Cockpit plugin api
   *
   * @return
   */
  public static File[] getPluginApi() {
    return getMavenDependency("org.camunda.bpm.webapp:camunda-webapp-plugin-api");
  }

  public static File[] getResteasyJaxRs() {

    return resolver()
        .artifact("org.jboss.resteasy:resteasy-jaxrs")
          .exclusion("org.apache.httpcomponents:httpclient")
          .exclusion("commons-httpclient:commons-httpclient")
          .exclusion("commons-io:commons-io")
          .resolveAsFiles();
  }

  public static File[] getFestAssertions() {
    return getMavenDependency("org.easytesting:fest-assert");
  }

  public static File[] getMavenDependency(String name) {
    return resolver().artifact(name).resolveAsFiles();
  }

  public static File[] getMavenDependencies(String... names) {
    return resolver().artifacts(names).resolveAsFiles();
  }

  public static WebArchive getCockpitWar(String archiveName) {
    String cockpitPkg = Cockpit.class.getPackage().getName();
    String commonPkg = AppRuntimeDelegate.class.getPackage().getName();

    final WebArchive archive =
        ShrinkWrap
          .create(WebArchive.class, archiveName)
            .addPackages(true, commonPkg)
            .addPackage(cockpitPkg)
            .addPackages(true, cockpitPkg + ".db")
            .addPackages(true, cockpitPkg + ".impl")
            .addPackages(true, cockpitPkg + ".plugin")
            .addPackages(true, cockpitPkg + ".test.sample.web")

            .addAsLibraries(getMavenDependencies("org.camunda.bpm:camunda-engine-rest-core:jar"))
            .addAsServiceProvider(ProcessEngineProvider.class, TestProcessEngineProvider.class);;

    TestContainer.addContainerSpecificResources(archive);

    return archive;
  }

  public static WebArchive getAdminWar(String archiveName) {
    String cockpitPkg = Cockpit.class.getPackage().getName();
    String adminPkg = Admin.class.getPackage().getName();
    String commonPkg = AppRuntimeDelegate.class.getPackage().getName();

    final WebArchive archive =
        ShrinkWrap
          .create(WebArchive.class, archiveName)

            .addPackages(true, commonPkg)

            .addPackage(cockpitPkg)
            .addPackages(true, cockpitPkg + ".db")
            .addPackages(true, cockpitPkg + ".impl")
            .addPackages(true, cockpitPkg + ".plugin")
            .addPackages(true, cockpitPkg + ".test.sample.web")

            .addPackages(true, adminPkg)

            .addAsLibraries(getMavenDependencies("org.camunda.bpm:camunda-engine-rest-core:jar"))
            .addAsServiceProvider(ProcessEngineProvider.class, TestProcessEngineProvider.class);


    TestContainer.addContainerSpecificResources(archive);

    return archive;
  }

  public static WebArchive getTasklistWar(String archiveName) {
    String cockpitPkg = Cockpit.class.getPackage().getName();
    String adminPkg = Admin.class.getPackage().getName();
    String tasklistPkg = Tasklist.class.getPackage().getName();
    String commonPkg = AppRuntimeDelegate.class.getPackage().getName();

    final WebArchive archive =
        ShrinkWrap
          .create(WebArchive.class, archiveName)

            .addPackages(true, commonPkg)

            .addPackage(cockpitPkg)
            .addPackages(true, cockpitPkg + ".db")
            .addPackages(true, cockpitPkg + ".impl")
            .addPackages(true, cockpitPkg + ".plugin")
            .addPackages(true, cockpitPkg + ".test.sample.web")

            .addPackages(true, adminPkg)

            .addPackages(true, tasklistPkg)

            .addAsLibraries(getMavenDependencies("org.camunda.bpm:camunda-engine-rest-core:jar"))
            .addAsServiceProvider(ProcessEngineProvider.class, TestProcessEngineProvider.class);


    TestContainer.addContainerSpecificResources(archive);

    return archive;
  }

  public static JavaArchive getTestProcessArchiveAsFiles() {
    JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test-process-application.jar")
        .addClass(TestProcessApplication.class);

    addFiles(archive, "", new File("src/test/resources/process-archive"));

    return archive;
  }

  public static File[] getTestProcessArchiveJar() {
    getTestProcessArchiveAsFiles().as(ZipExporter.class)
        .exportTo(new File("target/test-process-archive.jar"), true);

    return new File[] { new File("target/test-process-archive.jar") };
  }

  public static File[] getCockpitTestPluginJar() {
    getCockpitTestPluginAsFiles().as(ZipExporter.class)
        .exportTo(new File("target/test-plugin.jar"), true);

    return new File[] { new File("target/test-plugin.jar") };
  }

  public static JavaArchive getCockpitTestPluginAsFiles() {
    JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test-plugin.jar")
        .addPackages(true, SimpleCockpitPlugin.class.getPackage())
        .addAsServiceProvider(CockpitPlugin.class, SimpleCockpitPlugin.class);

    String pkgName = SimpleCockpitPlugin.class.getPackage().getName().replaceAll("\\.", "/");

    addFiles(archive, pkgName, new File("src/test/resources/" + pkgName));
    addFiles(archive, "plugin-webapp", new File("src/test/resources/plugin-webapp"));

    return archive;
  }

  public static File[] getAdminTestPluginJar() {
    getAdminTestPluginAsFiles().as(ZipExporter.class)
        .exportTo(new File("target/test-plugin.jar"), true);

    return new File[] { new File("target/test-plugin.jar") };
  }

  public static JavaArchive getAdminTestPluginAsFiles() {
    JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test-plugin.jar")
        .addPackages(true, SimpleAdminPlugin.class.getPackage())
        .addAsServiceProvider(AdminPlugin.class, SimpleAdminPlugin.class);

    addFiles(archive, "plugin-webapp", new File("src/test/resources/plugin-webapp"));

    return archive;
  }

  public static File[] getTasklistTestPluginJar() {
    getTasklistTestPluginAsFiles().as(ZipExporter.class)
        .exportTo(new File("target/test-plugin.jar"), true);

    return new File[] { new File("target/test-plugin.jar") };
  }

  public static JavaArchive getTasklistTestPluginAsFiles() {
    JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test-plugin.jar")
        .addPackages(true, SimpleTasklistPlugin.class.getPackage())
        .addAsServiceProvider(TasklistPlugin.class, SimpleTasklistPlugin.class);

    addFiles(archive, "plugin-webapp", new File("src/test/resources/plugin-webapp"));

    return archive;
  }

  public static void addFiles(JavaArchive archive, String prefix, File dir) {
    if (!dir.isDirectory()) {
      throw new IllegalArgumentException("not a directory");
    }

    addFiles(archive, prefix, dir.getPath(), dir);
  }

  private static void addFiles(JavaArchive archive, String prefix, String rootPath, File dir) {

    for (File f : dir.listFiles()) {
      if (f.isFile()) {
        String filePath = f.getPath().replace(rootPath, "").replace("\\", "/");

        archive.addAsResource(f, prefix + filePath);
      } else {
        addFiles(archive, prefix, rootPath, f);
      }
    }
  }
}

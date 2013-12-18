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

import org.camunda.bpm.cockpit.test.sample.TestProcessApplication;
import java.io.File;

import org.camunda.bpm.cockpit.Cockpit;
import org.camunda.bpm.cockpit.core.test.util.TestContainer;
import org.camunda.bpm.cockpit.plugin.spi.CockpitPlugin;
import org.camunda.bpm.cockpit.plugin.test.application.TestProcessEngineProvider;
import org.camunda.bpm.cockpit.test.sample.plugin.simple.SimplePlugin;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;
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

    final WebArchive archive =
        ShrinkWrap
          .create(WebArchive.class, archiveName)
            .addPackage(cockpitPkg)
            .addPackages(true, cockpitPkg + ".db")
            .addPackages(true, cockpitPkg + ".impl")
            .addPackages(true, cockpitPkg + ".plugin")
            .addPackages(true, cockpitPkg + ".test.sample.web")
            .addAsLibraries(getMavenDependencies("org.camunda.bpm:camunda-engine-rest:jar:classes"))
            .addAsServiceProvider(ProcessEngineProvider.class, TestProcessEngineProvider.class);;

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

  public static JavaArchive getTestPluginAsFiles() {
    JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "test-plugin.jar")
        .addPackages(true, SimplePlugin.class.getPackage())
        .addAsServiceProvider(CockpitPlugin.class, SimplePlugin.class);

    String pkgName = SimplePlugin.class.getPackage().getName().replaceAll("\\.", "/");

    addFiles(archive, pkgName, new File("src/test/resources/" + pkgName));
    addFiles(archive, "plugin-webapp", new File("src/test/resources/plugin-webapp"));

    return archive;
  }

  public static File[] getTestPluginJar() {
    getTestPluginAsFiles().as(ZipExporter.class)
        .exportTo(new File("target/test-plugin.jar"), true);

    return new File[] { new File("target/test-plugin.jar") };
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

package org.camunda.bpm.integrationtest.deployment.callbacks;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.cdi.impl.util.ProgrammaticBeanLookup;
import org.camunda.bpm.engine.impl.ManagementServiceImpl;
import org.camunda.bpm.engine.impl.management.DatabasePurgeReport;
import org.camunda.bpm.engine.impl.management.PurgeReport;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.CachePurgeResult;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@WebServlet(urlPatterns = "/purge")
public class PurgeDatabaseServlet extends HttpServlet {

  protected static final String DATABASE_NOT_CLEAN = "Database was not clean!\n";
  protected static final String CACHE_IS_NOT_CLEAN = "Cache was not clean!\n";
  protected Logger logger = Logger.getLogger(PurgeDatabaseServlet.class.getName());

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    logger.log(Level.INFO, "=== PurgeDatabaseServlet ===");
    ProcessEngine engine = ProgrammaticBeanLookup.lookup(ProcessEngine.class);
    ManagementServiceImpl managementService = (ManagementServiceImpl) engine.getManagementService();
    PurgeReport report = managementService.purge();

    if (report.isEmpty()) {
      logger.log(Level.INFO, "Clean DB and cache.");
      resp.setStatus(201);
    } else {
      resp.setStatus(400);
      PrintWriter writer = resp.getWriter();

      DatabasePurgeReport databasePurgeReport = report.getDatabasePurgeReport();
      if (!databasePurgeReport.isEmpty()) {
        logger.log(Level.INFO, DATABASE_NOT_CLEAN);
        writer.append(DATABASE_NOT_CLEAN).append(databasePurgeReport.getPurgeReportAsString());
      }

      CachePurgeResult cachePurgeResult = report.getCachePurgeResult();
      if (!cachePurgeResult.isEmpty()) {
        logger.log(Level.INFO, CACHE_IS_NOT_CLEAN);
        writer.append(CACHE_IS_NOT_CLEAN).append(cachePurgeResult.getPurgeReportAsString());
      }
    }
  }
}

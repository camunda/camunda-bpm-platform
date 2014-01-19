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
package org.camunda.bpm.qa.performance.engine.framework.report;

import java.util.List;

import org.camunda.bpm.engine.impl.form.engine.HtmlDocumentBuilder;
import org.camunda.bpm.engine.impl.form.engine.HtmlElementWriter;
import org.camunda.bpm.qa.performance.engine.framework.aggregate.TabularResultSet;

/**
 * Uses a {@link TabularResultSet} and renders it as a Html Table.
 *
 * @author Daniel Meyer
 *
 */
public class HtmlReportBuilder {

  protected TabularResultSet resultSet;
  protected String resultsBaseFolder;
  protected String jsonSourceFileName;
  protected String reportName;

  public HtmlReportBuilder(TabularResultSet resultSet) {
    this.resultSet = resultSet;
  }

  public HtmlReportBuilder resultDetailsFolder(String resultsBaseFolder) {
    this.resultsBaseFolder = resultsBaseFolder;
    return this;
  }

  public HtmlReportBuilder jsonSource(String jsonSourceFileName) {
    this.jsonSourceFileName = jsonSourceFileName;
    return this;
  }

  public HtmlReportBuilder name(String reportName) {
    this.reportName = reportName;
    return this;
  }

  public String execute() {

    HtmlDocumentBuilder documentBuilder = new HtmlDocumentBuilder(new HtmlElementWriter("html"));

    /** <head>...</head> */
    documentBuilder.startElement(new HtmlElementWriter("head"))
      .startElement(new HtmlElementWriter("title").textContent(reportName))
      .endElement()
      .startElement(new HtmlElementWriter("link")
          .attribute("rel", "stylesheet")
          .attribute("href", "http://netdna.bootstrapcdn.com/bootstrap/3.0.3/css/bootstrap.min.css"))
      .endElement()
      .startElement(new HtmlElementWriter("link")
          .attribute("rel", "stylesheet")
          .attribute("href", "http://netdna.bootstrapcdn.com/bootstrap/3.0.3/css/bootstrap-theme.min.css"))
      .endElement()
    .endElement();

    /** <body> */
    HtmlDocumentBuilder bodyBuilder = documentBuilder.startElement(new HtmlElementWriter("body"))
        .startElement(new HtmlElementWriter("div").attribute("class", "container"));

    /** build Headline row */
    bodyBuilder
      .startElement(new HtmlElementWriter("div").attribute("class", "row"))
        .startElement(new HtmlElementWriter("div").attribute("class", "coll-md-12"))
          .startElement(new HtmlElementWriter("h1").textContent(reportName))
          .endElement()
        .endElement()
      .endElement();

    if(jsonSourceFileName != null) {

      bodyBuilder
        .startElement(new HtmlElementWriter("div").attribute("class", "row"))
          .startElement(new HtmlElementWriter("div").attribute("class", "coll-md-12"))
            .startElement(new HtmlElementWriter("p"))
              .startElement(new HtmlElementWriter("a")
                  .attribute("href", jsonSourceFileName)
                  .textContent("This Report as JSON"))
            .endElement()
            .endElement()
          .endElement()
        .endElement();

    }

    bodyBuilder
      .startElement(new HtmlElementWriter("div").attribute("class", "row"))
        .startElement(new HtmlElementWriter("div").attribute("class", "coll-md-12"));

        writeResultTable(bodyBuilder);

    bodyBuilder
        .endElement()
      .endElement();


    /** </body> */
    bodyBuilder.endElement()
      .endElement();

    return documentBuilder
      .endElement()
      .getHtmlString();

  }

  protected void writeResultTable(HtmlDocumentBuilder bodyBuilder) {

    /** <table> */
    HtmlDocumentBuilder tableBuilder = bodyBuilder.startElement(new HtmlElementWriter("table").attribute("class", "table table-condensed"));

    /** <tr> */
    HtmlDocumentBuilder tableHeadRowBuilder = tableBuilder.startElement(new HtmlElementWriter("tr"));

    for (String columnName : resultSet.getResultColumnNames()) {
      tableHeadRowBuilder.startElement(new HtmlElementWriter("th").textContent(columnName))
       .endElement();
    }

    if(resultsBaseFolder != null) {
      tableHeadRowBuilder.startElement(new HtmlElementWriter("th", true)).endElement();
    }

    /** </tr> */
    tableHeadRowBuilder.endElement();

    for (List<Object> resultRow : resultSet.getResults()) {

      /** <tr> */
      HtmlDocumentBuilder tableRowBuilder = tableBuilder.startElement(new HtmlElementWriter("tr"));

      for (Object resultColumn : resultRow) {
        tableHeadRowBuilder.startElement(new HtmlElementWriter("td").textContent(String.valueOf(resultColumn)))
          .endElement();
      }

      if(resultsBaseFolder != null) {
        /** build link to Json file */
        tableHeadRowBuilder
          .startElement(new HtmlElementWriter("td"))
            .startElement(new HtmlElementWriter("a")
                            .attribute("href", resultsBaseFolder + resultRow.get(0)+".json")
                            .textContent("details"))
            .endElement()
        .endElement();
      }

      /** </tr> */
      tableRowBuilder.endElement();

    }

    /** </table> */
    tableBuilder.endElement();
  }

}

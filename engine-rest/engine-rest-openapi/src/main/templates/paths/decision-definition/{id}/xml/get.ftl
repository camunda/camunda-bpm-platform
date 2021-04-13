<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getDecisionDefinitionDmnXmlById"
      tag = "Decision Definition"
      summary = "Get XML By Id"
      desc = "Retrieves the DMN XML of a decision definition." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the decision definition." />


  ],
  "responses" : {
    <@lib.response
        code = "200"
        dto = "DecisionDefinitionDiagramDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 response",
                       "description": "Response for GET `/decision-definition/aDecisionDefinitionId/xml`",
                       "value": {
                         "id": "aDecisionDefinitionId",
                         "dmnXml": "<?xml version=\\"1.0\\" encoding=\\"UTF-8\\"?>
                                    <definitions xmlns=\\"http://www.omg.org/spec/DMN/20151101/dmn.xsd\\"
                                                 id=\\"definitions\\"
                                                 name=\\"camunda\\"
                                                 namespace=\\"http://camunda.org/schema/1.0/dmn\\">
                                      <decision id=\\"testDecision\\" name=\\"decision\\">
                                        <decisionTable id=\\"table\\">
                                          <output id=\\"result\\" name=\\"result\\" >
                                          </output>
                                          <rule id=\\"rule\\">
                                            <outputEntry id=\\"output1\\">
                                              <text>\\"not okay\\"</text>
                                            </outputEntry>
                                          </rule>
                                        </decisionTable>
                                      </decision>
                                    </definitions>"
                           }
                        }'] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Decision definition with given id does not exist.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>
  }
}
</#macro>
<#-- Generated From File: camunda-docs-manual/public/reference/rest/decision-requirements-definition/get-xml/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getDecisionRequirementsDefinitionDmnXmlByKey"
      tag = "Decision Requirements Definition"
      summary = "Get DMN XML by Key"
      desc = "Retrieves the DMN XML of a decision requirements definition.
              Returns the XML for the latest version of the decision requirements 
              definition which belongs to no tenant."
  />

  "parameters" : [

      <@lib.parameter
          name = "key"
          location = "path"
          type = "string"
          required = true
          desc = "The key of the decision requirements definition (the latest version thereof) to be
                  retrieved."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "DecisionRequirementsDefinitionXmlDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/decision-requirements-definition/key/invoiceKey/xml`",
                       "description": "GET `/decision-requirements-definition/key/invoiceKey/xml`",
                       "value": {
                         "id": "invoice:1:9f86d61f-9ee5-11e3-be3b-606720b6f99c",
                         "dmnXml": "<?xml version=\\"1.1\\" encoding=\\"UTF-8\\"?>...<definitions id=\\"dish\\" name=\\"Dish\\" namespace=\\"test-drg\\" ... />"
                       }
                     }']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "
                Decision requirements definition with given id or key does not
                exist.
                      See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format.
                "
        last = true
    />

  }

}
</#macro>

<#-- Generated From File: camunda-docs-manual/public/reference/rest/decision-requirements-definition/get/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getDecisionRequirementsDefinitionById"
      tag = "Decision Requirements Definition"
      summary = "Get Decision Requirements Definition by ID"
      desc = "Retrieves a decision requirements definition according to the
              `DecisionRequirementsDefinition` interface in the engine."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the decision requirements definition to be retrieved."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "DecisionRequirementsDefinitionDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/decision-requirements-definition/invoice:1:9f86d61f-9ee5-11e3-be3b-606720b6f99c`",
                       "description": "GET `/decision-requirements-definition/invoice:1:9f86d61f-9ee5-11e3-be3b-606720b6f99c`",
                       "value": {
                         "id": "invoice:1:9f86d61f-9ee5-11e3-be3b-606720b6f99c",
                         "key": "invoiceKey",
                         "category": "invoice",
                         "name": "receiptInvoice",
                         "version": 2,
                         "resource": "invoice.dmn",
                         "deploymentId": "c627175e-41b7-11e6-b0ef-00aa004d0001",
                         "tenantId": null
                       }
                     }']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Decision requirements definition with given id or key does not
                exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>
<#-- Generated From File: camunda-docs-manual/public/reference/rest/decision-requirements-definition/get-diagram/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getDecisionRequirementsDefinitionDiagramByKeyAndTenantId"
      tag = "Decision Requirements Definition"
      summary = "Get Decision Requirements Diagram by Key and Tenant ID"
      desc = "Retrieves the diagram of a decision requirements definition.
              Returns the diagram of the latest version of the decision requirements 
              definition for a tenant."
  />

  "parameters" : [

      <@lib.parameter
          name = "key"
          location = "path"
          type = "string"
          required = true
          desc = "The key of the decision requirements definition (the latest version thereof) to be
                  retrieved."
      />

      <@lib.parameter
          name = "tenant-id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the tenant to which the decision requirements definition belongs to."
          last = true
      />

  ],

  "responses": {

    <@lib.multiTypeResponse
        code = "200"
        types = [
          {
            "binary": true,
            "mediaType": "image/*",
            "examples": ['"example-1": {
                       "summary": "GET `/decision-requirements-definition/key/invoiceKey/tenant-id/tenantA/diagram`",
                       "description": "GET `/decision-requirements-definition/key/invoiceKey/tenant-id/tenantA/diagram`",
                       "value": ""
                     }']
          },
          {
            "binary": true,
            "mediaType": "application/octet-stream",
            "examples": ['"example-1": {
                       "summary": "GET `/decision-requirements-definition/key/invoiceKey/tenant-id/tenantA/diagram`",
                       "description": "GET `/decision-requirements-definition/key/invoiceKey/tenant-id/tenantA/diagram`",
                       "value": ""
                     }']
          }
        ]
        desc = "The image diagram of the decision requirements definition."
    />

    <@lib.response
        code = "204"
        desc = "The decision requirements definition doesn't have an associated diagram. This
                method returns no content."
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

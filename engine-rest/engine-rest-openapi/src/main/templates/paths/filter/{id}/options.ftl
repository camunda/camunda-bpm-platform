<#-- Generated From File: camunda-docs-manual/public/reference/rest/filter/options/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "filterResourceOptionsSingle"
      tag = "Filter"
      summary = "Filter Resource Options"
      desc = "The OPTIONS request allows you to check for the set of available operations 
              that the currently authenticated user can perform on the `/filter` resource.
              Whether the user can perform an operation or not may depend on various
              factors, including the users authorizations to interact with this
              resource and the internal configuration of the process engine."
  />
  
  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the filter to be checked."
          last = true
      />
  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "ResourceOptionsDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "request",
                       "description": "OPTIONS `/filter/aFilterId`",
                       "value": {
                         "links":[
                           {
                             "method": "GET", 
                             "href":"http://localhost:8080/engine-rest/filter/aFilterId", 
                             "rel":"self"
                           },
                           {
                             "method": "GET", 
                             "href":"http://localhost:8080/engine-rest/filter/aFilterId/singleResult", 
                             "rel":"singleResult"
                           },
                           {
                             "method": "POST", 
                             "href":"http://localhost:8080/engine-rest/filter/aFilterId/singleResult", 
                             "rel":"singleResult"
                           },
                           {
                             "method": "GET", 
                             "href":"http://localhost:8080/engine-rest/filter/aFilterId/list", 
                             "rel":"list"
                           },
                           {
                             "method": "POST", 
                             "href":"http://localhost:8080/engine-rest/filter/aFilterId/list", 
                             "rel":"list"
                           },
                           {
                             "method": "GET", 
                             "href":"http://localhost:8080/engine-rest/filter/aFilterId/count", 
                             "rel":"count"
                           },
                           {
                             "method": "POST", 
                             "href":"http://localhost:8080/engine-rest/filter/aFilterId/count", 
                             "rel":"count"
                           },
                           {
                             "method": "PUT", 
                             "href":"http://localhost:8080/engine-rest/filter/aFilterId", 
                             "rel":"update"
                           },
                           {
                             "method": "DELETE", 
                             "href":"http://localhost:8080/engine-rest/filter/aFilterId", 
                             "rel":"delete"
                           }
                         ]
                       }
                   }']
        last = true
    />

  }

}
</#macro>
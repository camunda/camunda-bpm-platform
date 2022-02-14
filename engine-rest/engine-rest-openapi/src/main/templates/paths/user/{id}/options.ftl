<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "availableUserOperations"
      tag = "User"
      summary = "Options"
      desc = "The `/user` resource supports two custom `OPTIONS` requests, one for the resource as such
              and one for individual user instances. The `OPTIONS` request allows checking for the set of
              available operations that the currently authenticated user can perform on the /user resource.
              If the user can perform an operation or not may depend on various things, including the user's
              authorizations to interact with this resource and the internal configuration of the process
              engine. `OPTIONS /user/{id}` returns available interactions on a resource instance." />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          last = true
          desc = "The id of the user to be deleted." />
  ],

  "responses" : {

    <@lib.response
        mediaType = "application/json"
        code = "200"
        dto = "ResourceOptionsDto"
        last = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/user/peter`",
                       "value": {
                         "links":[
                          {"method":"GET","href":"http://localhost:8080/engine-rest/user/peter/profile","rel":"self"},
                          {"method":"DELETE","href":"http://localhost:8080/engine-rest/user/peter","rel":"delete"},
                          {"method":"PUT","href":"http://localhost:8080/engine-rest/user/peter/profile","rel":"update"}
                          ]
                          }
                     }'] />
    }
}

</#macro>
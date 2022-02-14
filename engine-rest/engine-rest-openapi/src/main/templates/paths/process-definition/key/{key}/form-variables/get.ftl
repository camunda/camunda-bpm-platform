<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getStartFormVariablesByKey"
      tag = "Process Definition"
      summary = "Get Start Form Variables"
      desc = "Retrieves the start form variables for the latest process definition which belongs to no tenant
              (only if they are defined via the 
              [Generated Task Form](${docsUrl}/user-guide/task-forms/#generated-task-forms) approach).
              The start form variables take form data specified on the start event into account.
              If form fields are defined, the variable types and default values
              of the form fields are taken into account." />

  "parameters" : [

    <@lib.parameter
        name = "key"
        location = "path"
        type = "string"
        required = true
        desc = "The key of the process definition (the latest version thereof) to be retrieved." />

    <@lib.parameter
        name = "variableNames"
        location = "query"
        type = "string"
        desc = "A comma-separated list of variable names. Allows restricting the list of requested
                variables to the variable names in the list. It is best practice to restrict the
                list of variables to the variables actually required by the form in order to
                minimize fetching of data. If the query parameter is ommitted all variables are
                fetched. If the query parameter contains non-existent variable names, the variable
                names are ignored." />

    <@lib.parameter
        name = "deserializeValues"
        location = "query"
        type = "boolean"
        defaultValue = "true"
        last = true
        desc = "Determines whether serializable variable values (typically variables that store
                custom Java objects) should be deserialized on server side (default true).

                If set to true, a serializable variable will be deserialized on server side and
                transformed to JSON using [Jackson's](http://jackson.codehaus.org/) POJO/bean
                property introspection feature. Note that this requires the Java classes of the
                variable value to be on the REST API's classpath.

                If set to false, a serializable variable will be returned in its serialized format.
                For example, a variable that is serialized as XML will be returned as a JSON string
                containing XML.

                **Note**: While true is the default value for reasons of backward compatibility, we
                recommend setting this parameter to false when developing web applications that are
                independent of the Java process applications deployed to the engine." />

  ],

  "responses" : {

    <@lib.response
        code = "200"
        additionalProperties = true
        dto = "VariableValueDto"
        desc = "Request successful. A JSON object containing a property for each variable returned."
        examples = ['"example-1": {
                       "summary": "Status 200 Response",
                       "description": "GET `/process-definition/anId/form-variables`",
                       "value": {
                         "amount": {
                             "type": "integer",
                             "value": 5,
                             "valueInfo": {}
                         },
                         "firstName": {
                             "type": "String",
                             "value": "Jonny",
                             "valueInfo": {}
                         }

                       }
                     }'] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "The key is null or does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>
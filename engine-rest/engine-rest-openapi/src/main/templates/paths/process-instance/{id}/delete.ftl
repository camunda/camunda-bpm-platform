{
  "operationId": "deleteProcessInstance",
  "description": "Deletes a running process instance by id.",
  "tags": [
    "Process instance"
  ],
  "parameters": [
    {
      "name": "id",
      "in": "path",
      "description": "The id of the process instance to be deleted.",
      "required": true,
      "schema": {
        "type": "string"
      }
    },
    {
      "name": "skipCustomListeners",
      "in": "query",
      "description": "If set to true, the custom listeners will be skipped.",
      "schema": {
        "type": "boolean",
        "default": false
      }
    },
    {
      "name": "skipIoMappings",
      "in": "query",
      "description": "If set to true, the input/output mappings will be skipped.",
      "schema": {
        "type": "boolean",
        "default": false
      }
    },
    {
      "name": "skipSubprocesses",
      "in": "query",
      "description": "If set to true, subprocesses related to deleted processes will be skipped.",
      "schema": {
        "type": "boolean",
        "default": false
      }
    },
    {
      "name": "failIfNotExists",
      "in": "query",
      "description": "If set to false, the request will still be successful if the process id is not found.",
      "schema": {
        "type": "boolean",
        "default": true
      }
    }
  ],
  "responses": {
     "204": {
       "description": "No Content"
     },
     "400": {
       "description": "Bad Request\n* If no name was given\n* If the variable value or type is invalid, for example if the value could not be parsed to an integer value or the passed variable type is not supported\n* If a tenant id and an execution id is specified",
       "content": {
         "application/json": {
           "schema": {
             "$ref": "#/components/schemas/ExceptionDto"
           }
         }
       }
     }
  }
}
<#macro dto_macro docsUrl="">
<@lib.dto>
 
    <@lib.property
      name = "data"
      type = "string"
      format = "binary"
      desc = "The binary data to be set.
              For File variables, this multipart can contain the filename, binary value and MIME type of the file variable to be set
              Only the filename is mandatory." />
 
    <@lib.property
      name = "valueType"
      type = "string"
      enumValues = ['"Bytes"', '"File"']
      last = true
      desc = "The name of the variable type. Either Bytes for a byte array variable or File for a file variable." />
 
    <#-- TODO deprecated properties, the problem is that the property id must be unique and here "data" property is repeating
      ,
    "type": {
      "type": "string",
      "deprecated": true,
      "description": "Deprecated: This only works if the REST API is aware of the involved Java classes.
                      The canonical java type name of the process variable to be set. Example: foo.bar.Customer.
                      If this part is provided, data must be a JSON object which can be converted into an instance of the provided class.
                      The content type of the data part must be application/json in that case (see above)."
    },
    "data": {
      "type": "string",
      "deprecated": true,
      "description": "**Deprecated**: This only works if the REST API is aware of the involved Java classes.
                      A JSON representation of a serialized Java Object. Form part type (see below) must be provided."
    } -->

</@lib.dto>
</#macro>
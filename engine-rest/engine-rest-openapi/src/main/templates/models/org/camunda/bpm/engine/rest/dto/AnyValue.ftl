<#macro dto_macro docsUrl="">
<#-- Note: This type can be any type. This type is required for ConditionQueryParameterDto, as it that Dto allows for
string, boolean and number variable values. The java client generator generates AnyValue as Object. Ideally, we could create a
type that is restricted to string, number and boolean, however, I could not get the client generator to generate
compilable java code when doing so. For more info see https://swagger.io/docs/specification/data-models/data-types/#any
-->
  {
    "description": "Can be any value - string, number, boolean, array or object.\n **Note**: Not every endpoint supports every type."
  }
</#macro>
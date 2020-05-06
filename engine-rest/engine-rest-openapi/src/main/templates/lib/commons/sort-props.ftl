<@lib.dto>

<#-- The sorting properties are defined in commons and are never "extended",
     but have to be explicitly `#include`-ed in a `Dto.ftl`. There are two
     reasons for this implementation:

      1. `sortBy` values must be dynamically defined. Changing the current
         `dto` macro requires a lot of work, but brings little value.
      2. Different `Query` endpoints have different `sort` properties. Not all
         properties (e.g. `parameters`) should be present everywhere. -->

    <@lib.property
        name = "sortBy"
        type = "string"
        enumValues = sortByValues
        desc = "Sort the results lexicographically by a given criterion.
                Must be used in conjunction with the sortOrder parameter." />

    <@lib.property
        name = "sortOrder"
        type = "string"
        enumValues = ['"asc"', '"desc"']
        last = last
        desc = "Sort the results in a given order. Values may be `asc` for ascending order or `desc` for
                descending order. Must be used in conjunction with the sortBy parameter." />

    <#if sortParamsDto?has_content>
      , <#-- add a comma (,) if the `last` flag is set -->


      <@lib.property
          name = "parameters"
          type = "ref"
          dto = "${sortParamsDto}"
          last = last />

    </#if>

</@lib.dto>
attribute = S(input).attr(attributeName);
name = attribute.name();
value = attribute.value();
namespace = attribute.namespace();
hasNullNamespace = attribute.hasNamespace(null);
newValue = attribute.value(valueToSet).value();

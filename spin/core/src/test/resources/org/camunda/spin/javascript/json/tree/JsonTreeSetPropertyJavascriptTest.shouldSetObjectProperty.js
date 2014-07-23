node = JSON(input);
var object = {};
object.name = "test";
object.comment = "42!";
node.prop("comment", object);

propertyNode = node.prop("comment");
value = propertyNode.prop("comment").stringValue();
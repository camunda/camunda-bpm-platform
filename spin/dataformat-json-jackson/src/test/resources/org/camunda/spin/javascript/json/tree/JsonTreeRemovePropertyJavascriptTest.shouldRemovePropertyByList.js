var node = S(input, "application/json");
var list = ["order", "active"];
node.deleteProp(list);

value1 = node.hasProp("order");
value2 = node.hasProp("active");
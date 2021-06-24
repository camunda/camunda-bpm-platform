node = S(input, "application/json");

oldValue = node.prop("order");

var list = [];
list.push("test");
list.push("test2");

node.prop("order", list);
newValue = node.prop("order");
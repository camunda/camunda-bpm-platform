node = S(input, "application/json");

oldValue = node.prop("order");

var object = {
  name: "test",
  comment: "42!"
};


node.prop("order", object);
newValue = node.prop("order");
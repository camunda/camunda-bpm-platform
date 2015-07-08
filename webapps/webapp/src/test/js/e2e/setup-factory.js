'use strict';
module.exports = {
  combine: function() {
    return Array.prototype.concat.apply([], arguments);
  },

  operation: function(module, operation, params) {
    var out = [];
    for(var i = 0; i < params.length; i++) {
      out.push({
        module: module,
        operation: operation,
        params: params[i]
      });
    }
    return out;
  }
};

define([], function() {
  return function(r, m, p) {
    for(var i = 0; i < m.length; i++) {
      (function(i) {
        define(m[i],function(){return p[m[i]];});
      })(i);
    }
  }
});

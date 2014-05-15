'use strict';
xdescribe('The task', function() {
  var taskModule;

  it('loads requirejs', function() {
    taskModule = require('client/scripts/task');
  });

  it('has what it needs', function() {
    console.info('Try to do something clever for once...', taskModule);
  });
});

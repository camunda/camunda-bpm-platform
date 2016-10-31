  'use strict';

  module.exports = [ 'ViewsProvider', function(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.decisionInstance.table', {
      id: 'highlightRules',
      initialize: function(data) {
        data.decisionInstance.outputs.forEach(function(output) {
          if (output.ruleId) {
            data.tableControl.highlightRow(output.ruleId, 'fired');
          }
        });
      }
    });
  }];

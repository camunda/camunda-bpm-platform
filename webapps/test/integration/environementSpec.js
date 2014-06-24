'use strict';
xdescribe('The - integration - environement', function() {
  describe('module loading', function() {
    var camundaTasklist;

    it('loads camunda-tasklist-ui.js', function() {
      runs(function() {
        require(['camunda-tasklist-ui'], function(loaded) {
          camundaTasklist = loaded;
        });
      });

      waitsFor(function() {
        return !!camundaTasklist;
      }, 400);

      runs(function() {
        expect(camundaTasklist).not.toBeUndefined();
      });
    });
  });
});

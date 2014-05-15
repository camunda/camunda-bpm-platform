'use strict';
describe('The - integration - environement', function() {
  describe('module loading', function() {
    var camundaTasklist;

    it('loads camunda-tasklist.js', function() {
      runs(function() {
        require(['camunda-tasklist'], function(loaded) {
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

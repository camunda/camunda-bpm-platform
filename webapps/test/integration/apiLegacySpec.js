'use strict';
describe('The legacy API', function() {
  var apiModule, ProcessDefinition, Q, $, angular;

  describe('module', function() {
    it('loads without blowing', function() {
      var loaded;
      runs(function() {
        // require.config({
        //   baseUrl: '/base',
        //   paths: {'camunda-tasklist': 'client/scripts'}
        // });
        require(['dist/scripts/deps', 'dist/scripts/camunda-tasklist', 'scripts/api/index'], function() {
          console.info('libraries are loaded');
          loaded = true;
        });
      });

      waitsFor(function() {
        return loaded;
      }, 400);

      runs(function() {
        expect(function() {
          apiModule = require('scripts/api/index');
          angular = require('angular');
          $ = require('jquery');
        }).not.toThrow();
      });
    });


    it('needs a "defer"', function() {
      expect(function() {
        Q = require('q');
      }).not.toThrow();
    });


    it('needs "jquery"', function() {
      expect(function() {
        $ = require('jquery');
      }).not.toThrow();
    });


    it('is a function', function() {
      expect(typeof apiModule).toBe('function');
    });
  });


  describe('factory', function() {
    it('does not register when the "baseUrl" configuration is missing', function() {
      expect(function() {
        apiModule.registerLegacy({
          name: 'processDefinition',
          // baseUrl: '/camunda/api/engine/engine/default',
          defer: Q.defer,
          actions: {}
        });
      }).toThrow();
    });


    it('does not register when the "name" configuration is missing', function() {
      expect(function() {
        apiModule.registerLegacy({
          // name: 'processDefinition',
          baseUrl: '/camunda/api/engine/engine/default',
          defer: Q.defer,
          actions: {}
        });
      }).toThrow();
    });


    it('does not register when the "defer" configuration is missing', function() {
      expect(function() {
        apiModule.registerLegacy({
          name: 'processDefinition',
          baseUrl: '/camunda/api/engine/engine/default',
          // defer: Q.defer,
          actions: {}
        });
      }).toThrow();
    });


    it('registers a resource', function() {
      expect(function() {
        ProcessDefinition = apiModule.registerLegacy({
          name: 'processDefinition',
          baseUrl: '/camunda/api/engine/engine/default',
          defer: Q.defer,

          actions: {
            list: {
              path: '/process-definition',
              returns: 'array'
            },

            count: {
              path: '/process-definition/count',
              returns: 'number'
            },

            start: {
              method: 'POST',
              path: '/process-definition/key/{{key}}/start',
              returns: 'object'
            }
          }
        });
      }).not.toThrow();
    });


    it('does not register a resource more than once', function() {
      expect(function() {
        apiModule.registerLegacy({
          name: 'processDefinition',
          baseUrl: '/camunda/api/engine/engine/default'
        });
      }).toThrow();
    });
  });


  describe('usage', function() {
    var apiResource;

    it('can be static', function() {
      expect(function() {
        apiResource = apiModule('processDefinition');
      }).not.toThrow();

      expect(apiResource).toBeTruthy();


      expect(ProcessDefinition).toBe(apiResource);


      // expect(function() {
      //   apiModule('processDefinition').list();
      // }).not.toThrow();
    });


    it('can be instanciated', function() {
      expect(function() {
        apiResource = new ProcessDefinition();
      }).not.toThrow();


      describe('an instance', function() {
        it('has registered methods', function() {
          expect(typeof apiResource.list).toBe('function');
        });


        it('returns promises', function() {
          expect(typeof apiResource.list().then).toBe('function');
        });
      });
    });
  });
});

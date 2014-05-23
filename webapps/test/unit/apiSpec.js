'use strict';
describe('The API', function() {
  var apiModule, User;

  describe('module', function() {
    it('loads without blowing', function() {
      expect(function() {
        apiModule = require('./../../client/scripts/api');
      }).not.toThrow();
    });

    it('is a function', function() {
      expect(typeof apiModule).toBe('function');
    });
  });


  describe('factory', function() {
    it('does not register when the "URL" configuration is missing', function() {
      expect(function() {
        apiModule.register({
          name: 'User'
        });
      }).toThrow();
    });


    it('does not register when the "name" configuration is missing', function() {
      expect(function() {
        apiModule.register({
          url: '/tasklist/users'
        });
      }).toThrow();
    });


    it('registers a resource', function() {
      expect(function() {
        User = apiModule.register({
          url: '/tasklist/users',
          name: 'User'
        });
      }).not.toThrow();
    });


    it('does not register a resource more than once', function() {
      expect(function() {
        apiModule.register({
          url: '/tasklist/users',
          name: 'User'
        });
      }).toThrow();
    });


    describe('resource', function() {
      var user;

      it('can be instanciated', function() {
        expect(function() {
          user = new User();
        }).not.toThrow();
      });


      describe('model', function() {
        describe('"query" method', function() {
          it('can be called', function() {
            expect(typeof User.query).toBe('function');
          });
        });


        describe('"create" method', function() {
          it('can be called', function() {
            expect(typeof User.create).toBe('function');
          });
        });
      });


      describe('instance', function() {
        describe('"fetch" method', function() {
          it('can be called', function() {
            expect(typeof user.fetch).toBe('function');
          });
        });


        describe('"save" method', function() {
          it('can be called', function() {
            expect(typeof user.save).toBe('function');
          });
        });


        describe('"delete" method', function() {
          it('can be called', function() {
            expect(typeof user.save).toBe('function');
          });
        });
      });


      describe('custom model methods', function() {
        var ModelMethods;

        it('can be set using the configuration', function() {
          expect(function() {
            ModelMethods = apiModule.register({
              url: '/model-methods',
              name: 'ModelMethods',
              modelMethods: {
                modelMethod: function() { return true; }
              }
            });
          }).not.toThrow();
        });


        it('can be called', function() {
          expect(typeof ModelMethods.modelMethod).toBe('function');
          expect(ModelMethods.modelMethod()).toBeTruthy();
        });
      });


      describe('custom instance methods', function() {
        var InstanceMethods, instance;

        it('can be set using the configuration', function() {
          expect(function() {
            InstanceMethods = apiModule.register({
              url: '/instance-methods',
              name: 'InstanceMethods',
              instanceMethods: {
                instanceMethod: function() { return true; }
              }
            });

            instance = new InstanceMethods();
          }).not.toThrow();
        });


        it('can be called', function() {
          expect(typeof instance.instanceMethod).toBe('function');
          expect(instance.instanceMethod()).toBeTruthy();
        });
      });
    });
  });


  describe('usage', function() {
    it('can be static', function() {
      expect(apiModule('User')).toBeTruthy();

      expect(function() {
        apiModule('User').create();
      }).not.toThrow();
    });
  });
});

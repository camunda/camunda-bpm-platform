'use strict';
describe('The API', function() {
  var apiModule, User;
  describe('module', function() {
    it('loads without blowing', function() {
      expect(function() {
        apiModule = require('./../../client/scripts/api');
      }).not.toThrow();
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
          console.info('User instance', user, Object.keys(user));
        }).not.toThrow();
      });


      describe('model', function() {
        describe('query method', function() {
          it('can be called', function() {
            expect(typeof User.query).toBe('function');
          });
        });


        describe('create method', function() {
          it('can be called', function() {
            expect(typeof User.create).toBe('function');
          });
        });
      });


      describe('instance', function() {
        describe('fetch method', function() {
          it('can be called', function() {
            expect(typeof user.fetch).toBe('function');
          });
        });


        describe('save method', function() {
          it('can be called', function() {
            expect(typeof user.save).toBe('function');
          });
        });


        describe('delete method', function() {
          it('can be called', function() {
            expect(typeof user.save).toBe('function');
          });
        });
      });
    });
  });


  xdescribe('use', function() {
    it('...', function() {

    });
  });
});

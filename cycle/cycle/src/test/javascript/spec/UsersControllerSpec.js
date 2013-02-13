describe("A UserController, when updating users", function() {
  var $httpBackend, $rootScope, App, $controller;

  beforeEach(inject(function($injector) {
    $httpBackend = $injector.get('$httpBackend');
    
    UserController();
    
    $rootScope = $injector.get('$rootScope');
    $controller = $injector.get('$controller');
    App = $injector.get('App');
    
    $httpBackend.when('POST', 'app/secured/resources/user/1').respond({ name: 'Klaus' });
    $httpBackend.when('GET', 'app/secured/resources/user').respond([ { name: 'Klaus' }, { name: 'Walter' } ]);
  }));
  
  it('should update user details before retrieving the updated list', function() {
    $httpBackend.expectGET('app/name');

    var scope = $rootScope.$new();
    var ctrl = $controller(UserController, {
      $scope: scope
    });
  
    $httpBackend.flush();
  
    expect($scope.users).toBe(true);
    $httpBackend.verifyNoOutstandingExpectation();
    $httpBackend.verifyNoOutstandingRequest();
  });

});
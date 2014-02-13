describe('admin dashboard', function() {

	function changeUserProfile(firstName, lastName, email) {
		element(by.model('profile.firstName')).clear();
    element(by.model('profile.lastName')).clear();
    element(by.model('profile.email')).clear();
    element(by.model('profile.firstName')).sendKeys(firstName);
    element(by.model('profile.lastName')).sendKeys(lastName);
    element(by.model('profile.email')).sendKeys(email);

    var submitButton = element(by.css("button[type='submit']"));
    submitButton.click();
	}

  it('should load the home page', function() {
    var ptor = protractor.getInstance();
    browser.get('camunda/app/admin/');
    
  });

  it('should validate credentials', function() {
    element(by.model('username')).clear();
    element(by.model('password')).clear();
    element(by.model('username')).sendKeys('jonny1');
    element(by.model('password')).sendKeys('jonny1');    

    var submitButton = element(by.css('.btn-primary.btn-large'));
    submitButton.click();
  });

  it('should select the first user', function() {

  	var items = element(by.repeater('user in userList').row(0).column('{{user.firstName}} {{user.lastName}}'));

  	expect(items.getText()).toEqual('Demo Demo');
  	
  	items.click();
  });

  it('should change user profile', function() {

 		changeUserProfile('Vogel','Strauß','vogel.strauß@hotmail.net')

    // validate change
		element(by.css('.navbar ul li:nth-child(1)')).click();		

  	var items = element(by.repeater('user in userList').row(0).column('{{user.firstName}} {{user.lastName}}'));
  	expect(items.getText()).toEqual('Vogel Strauß');     
  	
  });

  it('should change user profile back to Demo Demo', function() {

  	var items = element(by.repeater('user in userList').row(0).column('{{user.firstName}} {{user.lastName}}'));
	
  	items.click();

		changeUserProfile('Demo','Demo','demo.demo@camunda.com')

    // validate change
		element(by.css('.navbar ul li:nth-child(1)')).click();		

  	var items = element(by.repeater('user in userList').row(0).column('{{user.firstName}} {{user.lastName}}'));
  	expect(items.getText()).toEqual('Demo Demo');     
  	
  });

});
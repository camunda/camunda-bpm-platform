var global = this;
describe('tasklist dashboard', function() {

  it('should load the home page', function() {
    var ptor = protractor.getInstance();
    browser.get('camunda/app/tasklist/');
    
/*    var ele = ptor.findElement(by.className('camunda Tasklist'));
    expect(ptor.isElementPresent(ele)).toBe(false);*/
  });

  it('should validate credentials', function() {
    element(by.model('username')).clear();
    element(by.model('password')).clear();
    element(by.model('username')).sendKeys('jonny1');
    element(by.model('password')).sendKeys('jonny1');    

    var submitButton = element(by.css('.btn-primary.btn-large'));
    submitButton.click();
  });

  it('should switch to admin', function() {
    element(by.css('.navbar [sem-show-apps]')).click();
    element(by.css('.navbar [sem-jump-to-admin]')).click();
  });

	it('should start a process instance', function() {
    element(by.css('.icon-th-list.icon-white')).click();
    
    var items = element(by.repeater('definition in processDefinitions').row(0).column('{{ definition.name || definition.key }}'));
    
    expect(items.getText()).toEqual('invoice receipt Generated Forms');
    items.click();

    element(by.css('*[name="firstname"]')).sendKeys('Metal');
    element(by.css('*[name="lastname"]')).sendKeys('Beppo'); 
    element(by.css('*[name="dateOfBirth"]')).sendKeys('13.01.1980'); 

    var startButton = element(by.css('.btn-primary'));
    startButton.click();    
  });
});
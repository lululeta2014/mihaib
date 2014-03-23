#! /usr/bin/env node

var webdriver = require('selenium-webdriver');
var driver = new webdriver.Builder()
    .withCapabilities(webdriver.Capabilities.chrome())
    .build();

driver.get('http://localhost:8082/elements/full-name/demo.html');
driver.findElement(webdriver.By.tagName('full-name')).then(function(e) {
    console.log('Found', e);
});
driver.findElement(webdriver.By.tagName('input')).then(function(e) {
    console.log('Found input element');
}, function(e) {
    console.log('Can\'t find input element');
});
driver.quit();

// Will continue when browsers implement these upcoming web standards.
// Must first find a way to select elements from the browser dev tools,
// maybe with a selector 'full-name /shadow/ input'.

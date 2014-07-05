var rule1 = {
    conditions: [
        new chrome.declarativeContent.PageStateMatcher({
            pageUrl: {
                hostContains: '.amazon.',
                pathContains: '/dp/'
            }
        }),
        new chrome.declarativeContent.PageStateMatcher({
            pageUrl: {
                hostContains: '.amazon.',
                pathContains: '/gp/product/'
            }
        })
    ],
    actions: [ new chrome.declarativeContent.ShowPageAction() ]
};

var rules = [rule1];

chrome.runtime.onInstalled.addListener(function(details) {
    chrome.declarativeContent.onPageChanged.removeRules(undefined, function() {
        chrome.declarativeContent.onPageChanged.addRules(rules);
    });
});

chrome.pageAction.onClicked.addListener(function(tab) {
    chrome.tabs.executeScript({
        'file': 'shorten.js'
    });
});

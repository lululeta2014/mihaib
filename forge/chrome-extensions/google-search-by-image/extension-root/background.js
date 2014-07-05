// https://developer.chrome.com/apps/contextMenus

chrome.runtime.onInstalled.addListener(function(details) {
    chrome.contextMenus.create({
        id: Math.random() + '',
        title: 'Image Search',
        contexts: ['image']
    });
});

chrome.contextMenus.onClicked.addListener(function(itemClicked, context) {
    // This isn't always a web URL, e.g. on Google Search and Image Search
    // pages, images have src="data:image/jpeg;base64…"
    chrome.tabs.create({
        url: ('https://www.google.com/searchbyimage?&image_url=' +
              encodeURIComponent(itemClicked.srcUrl))
    });
});

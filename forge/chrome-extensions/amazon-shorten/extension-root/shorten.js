(function() {
    var pathname = window.location.pathname;

    var prefixes = ['/dp/', '/gp/product/'], prefix, startIdx = -1;
    for (var i = 0; i < prefixes.length && startIdx == -1; i++) {
        prefix = prefixes[i];
        startIdx = pathname.indexOf(prefix);
    }
    if (startIdx == -1) {
        return;
    }

    var codeStartIdx = startIdx + prefix.length;
    var codeEndIdx = pathname.indexOf('/', codeStartIdx);
    if (codeEndIdx == -1) {
        return;
    }
    var code = pathname.substring(codeStartIdx, codeEndIdx);
    // Assigning separately to .pathname, .search and .hash doesn't work.
    // The last assigned field survives, the others are reset.
    window.location.href = prefix + code;
})();

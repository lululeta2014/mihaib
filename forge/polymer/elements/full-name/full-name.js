Polymer('full-name', {
    observe: {
        _first: 'computeFull',
        _last: 'computeFull',
        firstTxt: 'computeFirst',
        lastTxt: 'computeLast'
    },

    // the contents of the input fields, so we don't sanitize as the user's
    // typing, possibly removing what he types (e.g. trailing whitespace
    // while he's typing “Mary Ann”).
    firstTxt: '',
    lastTxt: '',

    // the sanitized values, computed only when their dependencies change
    _first: '',
    _last: '',
    _full: '',

    computeFirst: function() {
        this._first = this.sanitizeSingleName(this.firstTxt);
    },
    computeLast: function() {
        this._last = this.sanitizeSingleName(this.lastTxt);
    },
    computeFull: function() {
        this._full = (this.first + ' ' + this.last).trim();
    },

    // The public API
    get first() {
        return this._first;
    },
    set first(v) {
        this.firstTxt = v;
    },

    get last() {
        return this._last;
    },
    set last(v) {
        this.lastTxt = v;
    },

    get full() {
        return this._full;
    },
    set full(v) {
        throw 'Writing the .full field is forbidden';
    },

    // utilities

    /**
     * Sanitize a single name (either first-name or last-name,
     * not a full-name).
     */
    sanitizeSingleName: function(singleName) {
        return singleName.trim().replace(/\s+/g, ' ');
    }
});

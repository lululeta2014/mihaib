Polymer('full-name', {
    first: '',
    last: '',

    // when first or last change, we run 2 functions (firstChanged and
    // computeFull). If running just 1, we could have used ‘observe’ instead.
    firstChanged: function() {
        if (this.first) {
            // if trimming changes the string, this *Changed will fire again
            this.first = this.first.trim();
        }
        this.computeFull();
    },
    lastChanged: function(oldV, newV) {
        if (newV) {
            // if trimming changes the string, this *Changed will fire again
            this.last = newV.trim();
        }
        this.computeFull();
    },

    _full: '',
    get full() {
        return this._full;
    },
    computeFull: function() {
        this._full = (this.first + ' ' + this.last).trim();
    },

    // Just something to unit-test, e.g. from QUnit
    characterCount: function() {
        function len(s) {
            return s ? s.length : 0;
        }
        return len(this.first) + len(this.last);
    },

    ready: function() {
        this.computeFull();
    }
});

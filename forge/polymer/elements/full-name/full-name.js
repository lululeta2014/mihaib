Polymer('full-name', {
    observe: {
        typedFirst: 'computeFirst',
        typedLast: 'computeLast',
        prettyFirst: 'computeFull',
        prettyLast: 'computeFull'
    },

    // the contents of the input fields, so we don't sanitize as the user's
    // typing, possibly removing what he types (e.g. trailing whitespace
    // while he's typing “Mary Ann”).
    typedFirst: '',
    typedLast: '',

    // the sanitized values, computed only when their dependencies change
    prettyFirst: '',
    prettyLast: '',
    prettyFull: '',

    computeFirst: function() {
        // can't hurt to protect for null
        this.prettyFirst = this.sanitizeSingleName(this.typedFirst || '');
    },
    computeLast: function() {
        this.prettyLast = this.sanitizeSingleName(this.typedLast || '');
    },
    computeFull: function() {
        // either name may be empty
        this.prettyFull = (this.prettyFirst + ' ' + this.prettyLast).trim();
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

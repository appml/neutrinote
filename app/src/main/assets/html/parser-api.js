var neutriNote = new function() {
    this.document = null;

    // Initialize
    this.init = function(document) {
        this.document = document;
    }

    // Prepare
    this.prepare = function() {
        // Setup meta
        var metaEl = this.document.createElement('meta');
        metaEl.name = 'viewport';
        metaEl.content = 'width=device-width, initial-scale=1.0';

        if (this.document.head.firstChild)
            this.document.head.insertBefore(metaEl, document.head.firstChild);
        else
            this.document.head.appendChild(metaEl);

        // Setup content
        var contentEl = this.document.getElementsByTagName('xmp')[0] || this.document.getElementsByTagName('textarea')[0];
        var newNode = this.document.createElement('div');
        newNode.className = 'container';
        newNode.id = 'content';
        this.document.body.replaceChild(newNode, contentEl);
    };

    // Get raw data
    this.getData = function() {
        var contentEl = this.document.getElementsByTagName('xmp')[0] || this.document.getElementsByTagName('textarea')[0];
        return contentEl.textContent || contentEl.innerText;
    };

    // Set content
    this.setContent = function(str) {
        this.document.getElementById('content').innerHTML = str;
    };

    // Apply code style
    this.applyCodeStyle = function(tag) {
        // Prettify
        var codeEls = this.document.getElementsByTagName(tag);
        for (var i=0, ii=codeEls.length; i<ii; i++) {
            var codeEl = codeEls[i];
            var lang = codeEl.className;
            codeEl.className = 'prettyprint lang-' + lang;
        }
    }

    // Apply table style
    this.applyTableStyle = function(tag) {
        var tableEls = document.getElementsByTagName(tag);
        for (var i=0, ii=tableEls.length; i<ii; i++) {
            var tableEl = tableEls[i];
            tableEl.className = 'table table-striped table-bordered';
        }
    }
}
var neutriNote_utils = new function() {
    const side_margin = 20;
    var nano_content;
    var old_scale;
    var new_scale;
    var original_width;
    var last_target;
    var last_old_scale;
    var last_new_scale;

    // Initialize
    this.init = function(document) {
        doc = document;
        doc.addEventListener('click', this.reflow);

        nano_content = doc.getElementById('nano_content');

        old_scale = 1;
        new_scale = 1;
        original_width = nano_content.getBoundingClientRect().width;
    }

    // Reflow
    this.reflow = function(e) {
        // Get element
        let target = e.target;

        // Reset width
        if ((old_scale == 1) && (new_scale == 1)) {
            target.style.width = original_width;
            last_old_scale = 1;
            last_new_scale = 1;
            return;
        }

        // Sanity check
        if ((target == last_target) && (old_scale == last_old_scale) && (new_scale == last_new_scale)) {
            target.style.width = original_width;
            last_old_scale = 1;
            last_new_scale = 1;
            return;
        }

        // Compute new width
        const bbox = target.getBoundingClientRect()
        const new_width = Math.floor(bbox.width * old_scale / new_scale) - 2 * side_margin;

        // Set width
        target.style.width = new_width + 'px';

        // Remember last target
        last_target = target;
        last_old_scale = old_scale;
        last_new_scale = new_scale;
    };

    // Reflow all
    this.reflowAll = function() {
        let target = doc.getElementById('nano_content');
        let scrollY = window.scrollY;

        // Reset width
        if ((old_scale == 1) && (new_scale == 1)) {
            target.style.width = original_width;
            last_old_scale = 1;
            last_new_scale = 1;
            return;
        }

        // Sanity check
        if ((target == last_target) && (old_scale == last_old_scale) && (new_scale == last_new_scale)) {
            target.style.width = original_width;
            last_old_scale = 1;
            last_new_scale = 1;
            return;
        }

        // Compute new width
        const bbox = target.getBoundingClientRect()
        const new_width = Math.floor(bbox.width * old_scale / new_scale) - 2 * side_margin;

        // Set width
        target.style.width = new_width + 'px';

        // Remember last target
        last_target = target;
        last_old_scale = old_scale;
        last_new_scale = new_scale;
    }

    // Update scale
    this.updateScale = function(oldScale, newScale) {
        old_scale = oldScale;
        new_scale = newScale;
        target = null;    // Reset target
    }
}

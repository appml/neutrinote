(function () {
  // A quick way to make sure we're only keeping span-level tags when we need to.
  // This isn't supposed to be foolproof. It's just a quick way to make sure we
  // keep all span-level tags returned by a pagedown converter. It should allow
  // all span-level tags through, with or without attributes.
  var inlineTags = new RegExp(['^(<\\/?(a|abbr|acronym|applet|area|b|basefont|',
                               'bdo|big|button|cite|code|del|dfn|em|figcaption|',
                               'font|i|iframe|img|input|ins|kbd|label|map|',
                               'mark|meter|object|param|progress|q|ruby|rp|rt|s|',
                               'samp|script|select|small|span|strike|strong|',
                               'sub|sup|textarea|time|tt|u|var|wbr)[^>]*>|',
                               '<(br)\\s?\\/?>)$'].join(''), 'i');

  /******************************************************************
   * Utility Functions                                              *
   *****************************************************************/

  // patch for ie7
  if (!Array.indexOf) {
    Array.prototype.indexOf = function(obj) {
      for (var i = 0; i < this.length; i++) {
        if (this[i] == obj) {
          return i;
        }
      }
      return -1;
    };
  }

  function trim(str) {
    return str.replace(/^\s+|\s+$/g, '');
  }

  function rtrim(str) {
    return str.replace(/\s+$/g, '');
  }

  // Remove one level of indentation from text. Indent is 4 spaces.
  function outdent(text) {
    return text.replace(new RegExp('^(\\t|[ ]{1,4})', 'gm'), '');
  }

  function contains(str, substr) {
    return str.indexOf(substr) != -1;
  }

  // Sanitize html, removing tags that aren't in the whitelist
  function sanitizeHtml(html, whitelist) {
    return html.replace(/<[^>]*>?/gi, function(tag) {
      return tag.match(whitelist) ? tag : '';
    });
  }

  // Merge two arrays, keeping only unique elements.
  function union(x, y) {
    var obj = {};
    for (var i = 0; i < x.length; i++)
       obj[x[i]] = x[i];
    for (i = 0; i < y.length; i++)
       obj[y[i]] = y[i];
    var res = [];
    for (var k in obj) {
      if (obj.hasOwnProperty(k))
        res.push(obj[k]);
    }
    return res;
  }

  // JS regexes don't support \A or \Z, so we add sentinels, as Pagedown
  // does. In this case, we add the ascii codes for start of text (STX) and
  // end of text (ETX), an idea borrowed from:
  // https://github.com/tanakahisateru/js-markdown-extra
  function addAnchors(text) {
    if(text.charAt(0) != '\x02')
      text = '\x02' + text;
    if(text.charAt(text.length - 1) != '\x03')
      text = text + '\x03';
    return text;
  }

  // Remove STX and ETX sentinels.
  function removeAnchors(text) {
    if(text.charAt(0) == '\x02')
      text = text.substr(1);
    if(text.charAt(text.length - 1) == '\x03')
      text = text.substr(0, text.length - 1);
    return text;
  }

  // Convert markdown within an element, retaining only span-level tags
  function convertSpans(text, extra) {
    return sanitizeHtml(convertAll(text, extra), inlineTags);
  }

  // Convert internal markdown using the stock pagedown converter
  function convertAll(text, extra) {
    var result = extra.blockGamutHookCallback(text);
    // We need to perform these operations since we skip the steps in the converter
    result = unescapeSpecialChars(result);
    result = result.replace(/~D/g, "$$").replace(/~T/g, "~");
    result = extra.previousPostConversion(result);
    return result;
  }

  // Convert escaped special characters
  function processEscapesStep1(text) {
    // Markdown extra adds two escapable characters, `:` and `|`
    return text.replace(/\\\|/g, '~I').replace(/\\:/g, '~i');
  }
  function processEscapesStep2(text) {
    return text.replace(/~I/g, '|').replace(/~i/g, ':');
  }

  // Duplicated from PageDown converter
  function unescapeSpecialChars(text) {
    // Swap back in all the special characters we've hidden.
    text = text.replace(/~E(\d+)E/g, function(wholeMatch, m1) {
      var charCodeToReplace = parseInt(m1);
      return String.fromCharCode(charCodeToReplace);
    });
    return text;
  }
  
  function slugify(text) {
    return text.toLowerCase()
    .replace(/\s+/g, '-') // Replace spaces with -
    .replace(/[^\w\-]+/g, '') // Remove all non-word chars
    .replace(/\-\-+/g, '-') // Replace multiple - with single -
    .replace(/^-+/, '') // Trim - from start of text
    .replace(/-+$/, ''); // Trim - from end of text
  }

  /*****************************************************************************
   * Markdown.Extra *
   ****************************************************************************/

  Markdown.Extra = function() {
    // For converting internal markdown (in tables for instance).
    // This is necessary since these methods are meant to be called as
    // preConversion hooks, and the Markdown converter passed to init()
    // won't convert any markdown contained in the html tags we return.
    this.converter = null;

    // Stores html blocks we generate in hooks so that
    // they're not destroyed if the user is using a sanitizing converter
    this.hashBlocks = [];
    
    // Stores footnotes
    this.footnotes = {};
    this.usedFootnotes = [];

    // Special attribute blocks for fenced code blocks and headers enabled.
    this.attributeBlocks = false;

    // Fenced code block options
    this.googleCodePrettify = false;
    this.highlightJs = false;

    // Table options
    this.tableClass = '';

    this.tabWidth = 4;
  };

  Markdown.Extra.init = function(converter, options) {
    // Each call to init creates a new instance of Markdown.Extra so it's
    // safe to have multiple converters, with different options, on a single page
    var extra = new Markdown.Extra();
    var postNormalizationTransformations = [];
    var preBlockGamutTransformations = [];
    var postSpanGamutTransformations = [];
    var postConversionTransformations = ["unHashExtraBlocks"];

    options = options || {};
    options.extensions = options.extensions || ["all"];
    if (contains(options.extensions, "all")) {
      options.extensions = ["tables", "fenced_code_gfm", "def_list", "attr_list", "footnotes", "smartypants", "strikethrough", "newlines"];
    }
    preBlockGamutTransformations.push("wrapHeaders");
    if (contains(options.extensions, "attr_list")) {
      postNormalizationTransformations.push("hashFcbAttributeBlocks");
      preBlockGamutTransformations.push("hashHeaderAttributeBlocks");
      postConversionTransformations.push("applyAttributeBlocks");
      extra.attributeBlocks = true;
    }
    if (contains(options.extensions, "fenced_code_gfm")) {
      // This step will convert fcb inside list items and blockquotes
      preBlockGamutTransformations.push("fencedCodeBlocks");
      // This extra step is to prevent html blocks hashing and link definition/footnotes stripping inside fcb
      postNormalizationTransformations.push("fencedCodeBlocks");
    }
    if (contains(options.extensions, "tables")) {
      preBlockGamutTransformations.push("tables");
    }
    if (contains(options.extensions, "def_list")) {
      preBlockGamutTransformations.push("definitionLists");
    }
    if (contains(options.extensions, "footnotes")) {
      postNormalizationTransformations.push("stripFootnoteDefinitions");
      preBlockGamutTransformations.push("doFootnotes");
      postConversionTransformations.push("printFootnotes");
    }
    if (contains(options.extensions, "smartypants")) {
      postConversionTransformations.push("runSmartyPants");
    }
    if (contains(options.extensions, "strikethrough")) {
      postSpanGamutTransformations.push("strikethrough");
    }
    if (contains(options.extensions, "newlines")) {
      postSpanGamutTransformations.push("newlines");
    }

    converter.hooks.chain("postNormalization", function(text) {
      return extra.doTransform(postNormalizationTransformations, text) + '\n';
    });

    converter.hooks.chain("preBlockGamut", function(text, blockGamutHookCallback) {
      // Keep a reference to the block gamut callback to run recursively
      extra.blockGamutHookCallback = blockGamutHookCallback;
      text = processEscapesStep1(text);
      text = extra.doTransform(preBlockGamutTransformations, text) + '\n';
      text = processEscapesStep2(text);
      return text;
    });

    converter.hooks.chain("postSpanGamut", function(text) {
      return extra.doTransform(postSpanGamutTransformations, text);
    });

    // Keep a reference to the hook chain running before doPostConversion to apply on hashed extra blocks
    extra.previousPostConversion = converter.hooks.postConversion;
    converter.hooks.chain("postConversion", function(text) {
      text = extra.doTransform(postConversionTransformations, text);
      // Clear state vars that may use unnecessary memory
      extra.hashBlocks = [];
      extra.footnotes = {};
      extra.usedFootnotes = [];
      return text;
    });

    if ("highlighter" in options) {
      extra.googleCodePrettify = options.highlighter === 'prettify';
      extra.highlightJs = options.highlighter === 'highlight';
    }

    if ("table_class" in options) {
      extra.tableClass = options.table_class;
    }

    extra.converter = converter;

    // Caller usually won't need this, but it's handy for testing.
    return extra;
  };

  // Do transformations
  Markdown.Extra.prototype.doTransform = function(transformations, text) {
    for(var i = 0; i < transformations.length; i++)
      text = this[transformations[i]](text);
    return text;
  };

  // Return a placeholder containing a key, which is the block's index in the
  // hashBlocks array. We wrap our output in a <p> tag here so Pagedown won't.
  Markdown.Extra.prototype.hashExtraBlock = function(block) {
    return '\n<p>~X' + (this.hashBlocks.push(block) - 1) + 'X</p>\n';
  };
  Markdown.Extra.prototype.hashExtraInline = function(block) {
    return '~X' + (this.hashBlocks.push(block) - 1) + 'X';
  };

  // Replace placeholder blocks in `text` with their corresponding
  // html blocks in the hashBlocks array.
  Markdown.Extra.prototype.unHashExtraBlocks = function(text) {
    var self = this;
    function recursiveUnHash() {
      var hasHash = false;
      text = text.replace(/(?:<p>)?~X(\d+)X(?:<\/p>)?/g, function(wholeMatch, m1) {
        hasHash = true;
        var key = parseInt(m1, 10);
        return self.hashBlocks[key];
      });
      if(hasHash === true) {
        recursiveUnHash();
      }
    }
    recursiveUnHash();
    return text;
  };

  // Wrap headers to make sure they won't be in def lists
  Markdown.Extra.prototype.wrapHeaders = function(text) {
    function wrap(text) {
      return '\n' + text + '\n';
    }
    text = text.replace(/^.+[ \t]*\n=+[ \t]*\n+/gm, wrap);
    text = text.replace(/^.+[ \t]*\n-+[ \t]*\n+/gm, wrap);
    text = text.replace(/^\#{1,6}[ \t]*.+?[ \t]*\#*\n+/gm, wrap);
    return text;
  };


  /******************************************************************
   * Attribute Blocks                                               *
   *****************************************************************/

  // TODO: use sentinels. Should we just add/remove them in doConversion?
  // TODO: better matches for id / class attributes
  var attrBlock = "\\{[ \\t]*((?:[#.][-_:a-zA-Z0-9]+[ \\t]*)+)\\}";
  var hdrAttributesA = new RegExp("^(#{1,6}.*#{0,6})[ \\t]+" + attrBlock + "[ \\t]*(?:\\n|0x03)", "gm");
  var hdrAttributesB = new RegExp("^(.*)[ \\t]+" + attrBlock + "[ \\t]*\\n" +
    "(?=[\\-|=]+\\s*(?:\\n|0x03))", "gm"); // underline lookahead
  var fcbAttributes =  new RegExp("^(```[^`\\n]*)[ \\t]+" + attrBlock + "[ \\t]*\\n" +
    "(?=([\\s\\S]*?)\\n```[ \\t]*(\\n|0x03))", "gm");

  // Extract headers attribute blocks, move them above the element they will be
  // applied to, and hash them for later.
  Markdown.Extra.prototype.hashHeaderAttributeBlocks = function(text) {

    var self = this;
    function attributeCallback(wholeMatch, pre, attr) {
      return '<p>~XX' + (self.hashBlocks.push(attr) - 1) + 'XX</p>\n' + pre + "\n";
    }

    text = text.replace(hdrAttributesA, attributeCallback);  // ## headers
    text = text.replace(hdrAttributesB, attributeCallback);  // underline headers
    return text;
  };

  // Extract FCB attribute blocks, move them above the element they will be
  // applied to, and hash them for later.
  Markdown.Extra.prototype.hashFcbAttributeBlocks = function(text) {
    // TODO: use sentinels. Should we just add/remove them in doConversion?
    // TODO: better matches for id / class attributes

    var self = this;
    function attributeCallback(wholeMatch, pre, attr) {
      return '<p>~XX' + (self.hashBlocks.push(attr) - 1) + 'XX</p>\n' + pre + "\n";
    }

    return text.replace(fcbAttributes, attributeCallback);
  };

  Markdown.Extra.prototype.applyAttributeBlocks = function(text) {
    var self = this;
    var blockRe = new RegExp('<p>~XX(\\d+)XX</p>[\\s]*' +
                             '(?:<(h[1-6]|pre)(?: +class="(\\S+)")?(>[\\s\\S]*?</\\2>))', "gm");
    text = text.replace(blockRe, function(wholeMatch, k, tag, cls, rest) {
      if (!tag) // no following header or fenced code block.
        return '';

      // get attributes list from hash
      var key = parseInt(k, 10);
      var attributes = self.hashBlocks[key];

      // get id
      var id = attributes.match(/#[^\s#.]+/g) || [];
      var idStr = id[0] ? ' id="' + id[0].substr(1, id[0].length - 1) + '"' : '';

      // get classes and merge with existing classes
      var classes = attributes.match(/\.[^\s#.]+/g) || [];
      for (var i = 0; i < classes.length; i++) // Remove leading dot
        classes[i] = classes[i].substr(1, classes[i].length - 1);

      var classStr = '';
      if (cls)
        classes = union(classes, [cls]);

      if (classes.length > 0)
        classStr = ' class="' + classes.join(' ') + '"';

      return "<" + tag + idStr + classStr + rest;
    });

    return text;
  };

  /******************************************************************
   * Tables                                                         *
   *****************************************************************/

  // Find and convert Markdown Extra tables into html.
  Markdown.Extra.prototype.tables = function(text) {
    var self = this;

    var leadingPipe = new RegExp(
      ['^'                         ,
       '[ ]{0,3}'                  , // Allowed whitespace
       '[|]'                       , // Initial pipe
       '(.+)\\n'                   , // $1: Header Row

       '[ ]{0,3}'                  , // Allowed whitespace
       '[|]([ ]*[-:]+[-| :]*)\\n'  , // $2: Separator

       '('                         , // $3: Table Body
         '(?:[ ]*[|].*\\n?)*'      , // Table rows
       ')',
       '(?:\\n|$)'                   // Stop at final newline
      ].join(''),
      'gm'
    );

    var noLeadingPipe = new RegExp(
      ['^'                         ,
       '[ ]{0,3}'                  , // Allowed whitespace
       '(\\S.*[|].*)\\n'           , // $1: Header Row

       '[ ]{0,3}'                  , // Allowed whitespace
       '([-:]+[ ]*[|][-| :]*)\\n'  , // $2: Separator

       '('                         , // $3: Table Body
         '(?:.*[|].*\\n?)*'        , // Table rows
       ')'                         ,
       '(?:\\n|$)'                   // Stop at final newline
      ].join(''),
      'gm'
    );

    text = text.replace(leadingPipe, doTable);
    text = text.replace(noLeadingPipe, doTable);

    // $1 = header, $2 = separator, $3 = body
    function doTable(match, header, separator, body, offset, string) {
      // remove any leading pipes and whitespace
      header = header.replace(/^ *[|]/m, '');
      separator = separator.replace(/^ *[|]/m, '');
      body = body.replace(/^ *[|]/gm, '');

      // remove trailing pipes and whitespace
      header = header.replace(/[|] *$/m, '');
      separator = separator.replace(/[|] *$/m, '');
      body = body.replace(/[|] *$/gm, '');

      // determine column alignments
      var alignspecs = separator.split(/ *[|] */);
      var align = [];
      for (var i = 0; i < alignspecs.length; i++) {
        var spec = alignspecs[i];
        if (spec.match(/^ *-+: *$/m))
          align[i] = ' align="right"';
        else if (spec.match(/^ *:-+: *$/m))
          align[i] = ' align="center"';
        else if (spec.match(/^ *:-+ *$/m))
          align[i] = ' align="left"';
        else align[i] = '';
      }

      // TODO: parse spans in header and rows before splitting, so that pipes
      // inside of tags are not interpreted as separators
      var headers = header.split(/ *[|] */);
      var colCount = headers.length;

      // build html
      var cls = self.tableClass ? ' class="' + self.tableClass + '"' : '';
      var html = ['<table', cls, '>\n', '<thead>\n', '<tr>\n'].join('');

      // build column headers.
      for (i = 0; i < colCount; i++) {
        var headerHtml = convertSpans(trim(headers[i]), self);
        html += ["  <th", align[i], ">", headerHtml, "</th>\n"].join('');
      }
      html += "</tr>\n</thead>\n";

      // build rows
      var rows = body.split('\n');
      for (i = 0; i < rows.length; i++) {
        if (rows[i].match(/^\s*$/)) // can apply to final row
          continue;

        // ensure number of rowCells matches colCount
        var rowCells = rows[i].split(/ *[|] */);
        var lenDiff = colCount - rowCells.length;
        for (var j = 0; j < lenDiff; j++)
          rowCells.push('');

        html += "<tr>\n";
        for (j = 0; j < colCount; j++) {
          var colHtml = convertSpans(trim(rowCells[j]), self);
          html += ["  <td", align[j], ">", colHtml, "</td>\n"].join('');
        }
        html += "</tr>\n";
      }

      html += "</table>\n";

      // replace html with placeholder until postConversion step
      return self.hashExtraBlock(html);
    }

    return text;
  };


  /******************************************************************
   * Footnotes                                                      *
   *****************************************************************/

  // Strip footnote, store in hashes.
  Markdown.Extra.prototype.stripFootnoteDefinitions = function(text) {
    var self = this;

    text = text.replace(
      /\n[ ]{0,3}\[\^(.+?)\]\:[ \t]*\n?([\s\S]*?)\n{1,2}((?=\n[ ]{0,3}\S)|$)/g,
      function(wholeMatch, m1, m2) {
        m1 = slugify(m1);
        m2 += "\n";
        m2 = m2.replace(/^[ ]{0,3}/g, "");
        self.footnotes[m1] = m2;
        return "\n";
      });

    return text;
  };


  // Find and convert footnotes references.
  Markdown.Extra.prototype.doFootnotes = function(text) {
    var self = this;
    if(self.isConvertingFootnote === true) {
      return text;
    }

    var footnoteCounter = 0;
    text = text.replace(/\[\^(.+?)\]/g, function(wholeMatch, m1) {
      var id = slugify(m1);
      var footnote = self.footnotes[id];
      if (footnote === undefined) {
        return wholeMatch;
      }
      footnoteCounter++;
      self.usedFootnotes.push(id);
      var html = '<a href="#fn:' + id + '" id="fnref:' + id
      + '" title="See footnote" class="footnote">' + footnoteCounter
      + '</a>';
      return self.hashExtraInline(html);
    });

    return text;
  };

  // Print footnotes at the end of the document
  Markdown.Extra.prototype.printFootnotes = function(text) {
    var self = this;

    if (self.usedFootnotes.length === 0) {
      return text;
    }

    text += '\n\n<div class="footnotes">\n<hr>\n<ol>\n\n';
    for(var i=0; i<self.usedFootnotes.length; i++) {
      var id = self.usedFootnotes[i];
      var footnote = self.footnotes[id];
      self.isConvertingFootnote = true;
      var formattedfootnote = convertSpans(footnote, self);
      delete self.isConvertingFootnote;
      text += '<li id="fn:'
        + id
        + '">'
        + formattedfootnote
        + ' <a href="#fnref:'
        + id
        + '" title="Return to article" class="reversefootnote">&#8617;</a></li>\n\n';
    }
    text += '</ol>\n</div>';
    return text;
  };


  /******************************************************************
  * Fenced Code Blocks  (gfm)                                       *
  ******************************************************************/

  // Find and convert gfm-inspired fenced code blocks into html.
  Markdown.Extra.prototype.fencedCodeBlocks = function(text) {
    function encodeCode(code) {
      code = code.replace(/&/g, "&amp;");
      code = code.replace(/</g, "&lt;");
      code = code.replace(/>/g, "&gt;");
      // These were escaped by PageDown before postNormalization
      code = code.replace(/~D/g, "$$");
      code = code.replace(/~T/g, "~");
      return code;
    }

    var self = this;
    text = text.replace(/(?:^|\n)```([^`\n]*)\n([\s\S]*?)\n```[ \t]*(?=\n)/g, function(match, m1, m2) {
      var language = trim(m1), codeblock = m2;

      // adhere to specified options
      var preclass = self.googleCodePrettify ? ' class="prettyprint"' : '';
      var codeclass = '';
      if (language) {
        if (self.googleCodePrettify || self.highlightJs) {
          // use html5 language- class names. supported by both prettify and highlight.js
          codeclass = ' class="language-' + language + '"';
        } else {
          codeclass = ' class="' + language + '"';
        }
      }

      var html = ['<pre', preclass, '><code', codeclass, '>',
                  encodeCode(codeblock), '</code></pre>'].join('');

      // replace codeblock with placeholder until postConversion step
      return self.hashExtraBlock(html);
    });

    return text;
  };


  /******************************************************************
  * SmartyPants                                                     *
  ******************************************************************/
  
  Markdown.Extra.prototype.educatePants = function(text) {
    var self = this;
    var result = '';
    var blockOffset = 0;
    // Here we parse HTML in a very bad manner
    text.replace(/(?:<!--[\s\S]*?-->)|(<)([a-zA-Z1-6]+)([^\n]*?>)([\s\S]*?)(<\/\2>)/g, function(wholeMatch, m1, m2, m3, m4, m5, offset) {
      var token = text.substring(blockOffset, offset);
      result += self.applyPants(token);
      self.smartyPantsLastChar = result.substring(result.length - 1);
      blockOffset = offset + wholeMatch.length;
      if(!m1) {
        // Skip commentary
        result += wholeMatch;
        return;
      }
      // Skip special tags
      if(!/code|kbd|pre|script|noscript|iframe|math|ins|del|pre/i.test(m2)) {
        m4 = self.educatePants(m4);
      }
      else {
        self.smartyPantsLastChar = m4.substring(m4.length - 1);
      }
      result += m1 + m2 + m3 + m4 + m5;
    });
    var lastToken = text.substring(blockOffset);
    result += self.applyPants(lastToken);
    self.smartyPantsLastChar = result.substring(result.length - 1);
    return result;
  };
    
  function revertPants(wholeMatch, m1) {
    var blockText = m1;
    blockText = blockText.replace(/&\#8220;/g, "\"");
    blockText = blockText.replace(/&\#8221;/g, "\"");
    blockText = blockText.replace(/&\#8216;/g, "'");
    blockText = blockText.replace(/&\#8217;/g, "'");
    blockText = blockText.replace(/&\#8212;/g, "---");
    blockText = blockText.replace(/&\#8211;/g, "--");
    blockText = blockText.replace(/&\#8230;/g, "...");
    return blockText;
  }
  
  Markdown.Extra.prototype.applyPants = function(text) {
    // Dashes
    text = text.replace(/---/g, "&#8212;").replace(/--/g, "&#8211;");
    // Ellipses
    text = text.replace(/\.\.\./g, "&#8230;").replace(/\.\s\.\s\./g, "&#8230;");
    // Backticks
    text = text.replace(/``/g, "&#8220;").replace (/''/g, "&#8221;");
    
    if(/^'$/.test(text)) {
      // Special case: single-character ' token
      if(/\S/.test(this.smartyPantsLastChar)) {
        return "&#8217;";
      }
      return "&#8216;";
    }
    if(/^"$/.test(text)) {
      // Special case: single-character " token
      if(/\S/.test(this.smartyPantsLastChar)) {
        return "&#8221;";
      }
      return "&#8220;";
    }

    // Special case if the very first character is a quote
    // followed by punctuation at a non-word-break. Close the quotes by brute force:
    text = text.replace (/^'(?=[!"#\$\%'()*+,\-.\/:;<=>?\@\[\\]\^_`{|}~]\B)/, "&#8217;");
    text = text.replace (/^"(?=[!"#\$\%'()*+,\-.\/:;<=>?\@\[\\]\^_`{|}~]\B)/, "&#8221;");

    // Special case for double sets of quotes, e.g.:
    //   <p>He said, "'Quoted' words in a larger quote."</p>
    text = text.replace(/"'(?=\w)/g, "&#8220;&#8216;");
    text = text.replace(/'"(?=\w)/g, "&#8216;&#8220;");

    // Special case for decade abbreviations (the '80s):
    text = text.replace(/'(?=\d{2}s)/g, "&#8217;");
    
    // Get most opening single quotes:
    text = text.replace(/(\s|&nbsp;|--|&[mn]dash;|&\#8211;|&\#8212;|&\#x201[34];)'(?=\w)/g, "$1&#8216;");
    
    // Single closing quotes:
    text = text.replace(/([^\s\[\{\(\-])'/g, "$1&#8217;");
    text = text.replace(/'(?=\s|s\b)/g, "&#8217;");

    // Any remaining single quotes should be opening ones:
    text = text.replace(/'/g, "&#8216;");
    
    // Get most opening double quotes:
    text = text.replace(/(\s|&nbsp;|--|&[mn]dash;|&\#8211;|&\#8212;|&\#x201[34];)"(?=\w)/g, "$1&#8220;");
    
    // Double closing quotes:
    text = text.replace(/([^\s\[\{\(\-])"/g, "$1&#8221;");
    text = text.replace(/"(?=\s)/g, "&#8221;");
    
    // Any remaining quotes should be opening ones.
    text = text.replace(/"/ig, "&#8220;");
    return text;
  };

  // Find and convert markdown extra definition lists into html.
  Markdown.Extra.prototype.runSmartyPants = function(text) {
    this.smartyPantsLastChar = '';
    text = this.educatePants(text);
    // Clean everything inside html tags (some of them may have been converted due to our rough html parsing)
    text = text.replace(/(<([a-zA-Z1-6]+)\b([^\n>]*?)(\/)?>)/g, revertPants);
    return text;
  };
  
  /******************************************************************
  * Definition Lists                                                *
  ******************************************************************/

  // Find and convert markdown extra definition lists into html.
  Markdown.Extra.prototype.definitionLists = function(text) {
    var wholeList = new RegExp(
      ['(\\x02\\n?|\\n\\n)'          ,
       '(?:'                         ,
         '('                         , // $1 = whole list
           '('                       , // $2
             '[ ]{0,3}'              ,
             '((?:[ \\t]*\\S.*\\n)+)', // $3 = defined term
             '\\n?'                  ,
             '[ ]{0,3}:[ ]+'         , // colon starting definition
           ')'                       ,
           '([\\s\\S]+?)'            ,
           '('                       , // $4
               '(?=\\0x03)'          , // \z
             '|'                     ,
               '(?='                 ,
                 '\\n{2,}'           ,
                 '(?=\\S)'           ,
                 '(?!'               , // Negative lookahead for another term
                   '[ ]{0,3}'        ,
                   '(?:\\S.*\\n)+?'  , // defined term
                   '\\n?'            ,
                   '[ ]{0,3}:[ ]+'   , // colon starting definition
                 ')'                 ,
                 '(?!'               , // Negative lookahead for another definition
                   '[ ]{0,3}:[ ]+'   , // colon starting definition
                 ')'                 ,
               ')'                   ,
           ')'                       ,
         ')'                         ,
       ')'
      ].join(''),
      'gm'
    );

    var self = this;
    text = addAnchors(text);

    text = text.replace(wholeList, function(match, pre, list) {
      var result = trim(self.processDefListItems(list));
      result = "<dl>\n" + result + "\n</dl>";
      return pre + self.hashExtraBlock(result) + "\n\n";
    });

    return removeAnchors(text);
  };

  // Process the contents of a single definition list, splitting it
  // into individual term and definition list items.
  Markdown.Extra.prototype.processDefListItems = function(listStr) {
    var self = this;

    var dt = new RegExp(
      ['(\\x02\\n?|\\n\\n+)'    , // leading line
       '('                      , // definition terms = $1
         '[ ]{0,3}'             , // leading whitespace
         '(?![:][ ]|[ ])'       , // negative lookahead for a definition
                                  //   mark (colon) or more whitespace
         '(?:\\S.*\\n)+?'       , // actual term (not whitespace)
       ')'                      ,
       '(?=\\n?[ ]{0,3}:[ ])'     // lookahead for following line feed
      ].join(''),                 //   with a definition mark
      'gm'
    );

    var dd = new RegExp(
      ['\\n(\\n+)?'              , // leading line = $1
       '('                       , // marker space = $2
         '[ ]{0,3}'              , // whitespace before colon
         '[:][ ]+'               , // definition mark (colon)
       ')'                       ,
       '([\\s\\S]+?)'            , // definition text = $3
       '(?=\\n*'                 , // stop at next definition mark,
         '(?:'                   , // next term or end of text
           '\\n[ ]{0,3}[:][ ]|'  ,
           '<dt>|\\x03'          , // \z
         ')'                     ,
       ')'
      ].join(''),
      'gm'
    );

    listStr = addAnchors(listStr);
    // trim trailing blank lines:
    listStr = listStr.replace(/\n{2,}(?=\\x03)/, "\n");

    // Process definition terms.
    listStr = listStr.replace(dt, function(match, pre, termsStr) {
      var terms = trim(termsStr).split("\n");
      var text = '';
      for (var i = 0; i < terms.length; i++) {
        var term = terms[i];
        // process spans inside dt
        term = convertSpans(trim(term), self);
        text += "\n<dt>" + term + "</dt>";
      }
      return text + "\n";
    });

    // Process actual definitions.
    listStr = listStr.replace(dd, function(match, leadingLine, markerSpace, def) {
      if (leadingLine || def.match(/\n{2,}/)) {
        // replace marker with the appropriate whitespace indentation
        def = Array(markerSpace.length + 1).join(' ') + def;
        // process markdown inside definition
        // TODO?: currently doesn't apply extensions
        def = outdent(def) + "\n\n";
        def = "\n" + convertAll(def, self) + "\n";
      } else {
        // convert span-level markdown inside definition
        def = rtrim(def);
        def = convertSpans(outdent(def), self);
      }

      return "\n<dd>" + def + "</dd>\n";
    });

    return removeAnchors(listStr);
  };


  /***********************************************************
  * Strikethrough                                            *
  ************************************************************/

  Markdown.Extra.prototype.strikethrough = function(text) {
    // Pretty much duplicated from _DoItalicsAndBold
    return text.replace(/([\W_]|^)~T~T(?=\S)([^\r]*?\S[\*_]*)~T~T([\W_]|$)/g,
      "$1<del>$2</del>$3");
  };


  /***********************************************************
  * New lines                                                *
  ************************************************************/

  Markdown.Extra.prototype.newlines = function(text) {
    // We have to ignore already converted newlines and line breaks in sub-list items
    return text.replace(/(<(?:br|\/li)>)?\n/g, function(wholeMatch, previousTag) {
      return previousTag ? wholeMatch : " <br>\n";
    });
  };
  
})();

//Copyright (C) 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

window['PR_SHOULD_USE_CONTINUATION']=true;var prettyPrintOne;var prettyPrint;(function(){var win=window;var FLOW_CONTROL_KEYWORDS=["break,continue,do,else,for,if,return,while"];var C_KEYWORDS=[FLOW_CONTROL_KEYWORDS,"auto,case,char,const,default,"+"double,enum,extern,float,goto,int,long,register,short,signed,sizeof,"+"static,struct,switch,typedef,union,unsigned,void,volatile"];var COMMON_KEYWORDS=[C_KEYWORDS,"catch,class,delete,false,import,"+"new,operator,private,protected,public,this,throw,true,try,typeof"];var CPP_KEYWORDS=[COMMON_KEYWORDS,"alignof,align_union,asm,axiom,bool,"+"concept,concept_map,const_cast,constexpr,decltype,"+"dynamic_cast,explicit,export,friend,inline,late_check,"+"mutable,namespace,nullptr,reinterpret_cast,static_assert,static_cast,"+"template,typeid,typename,using,virtual,where"];var JAVA_KEYWORDS=[COMMON_KEYWORDS,"abstract,boolean,byte,extends,final,finally,implements,import,"+"instanceof,null,native,package,strictfp,super,synchronized,throws,"+"transient"];var CSHARP_KEYWORDS=[JAVA_KEYWORDS,"as,base,by,checked,decimal,delegate,descending,dynamic,event,"+"fixed,foreach,from,group,implicit,in,interface,internal,into,is,let,"+"lock,object,out,override,orderby,params,partial,readonly,ref,sbyte,"+"sealed,stackalloc,string,select,uint,ulong,unchecked,unsafe,ushort,"+"var,virtual,where"];var COFFEE_KEYWORDS="all,and,by,catch,class,else,extends,false,finally,"+"for,if,in,is,isnt,loop,new,no,not,null,of,off,on,or,return,super,then,"+"throw,true,try,unless,until,when,while,yes";var JSCRIPT_KEYWORDS=[COMMON_KEYWORDS,"debugger,eval,export,function,get,null,set,undefined,var,with,"+"Infinity,NaN"];var PERL_KEYWORDS="caller,delete,die,do,dump,elsif,eval,exit,foreach,for,"+"goto,if,import,last,local,my,next,no,our,print,package,redo,require,"+"sub,undef,unless,until,use,wantarray,while,BEGIN,END";var PYTHON_KEYWORDS=[FLOW_CONTROL_KEYWORDS,"and,as,assert,class,def,del,"+"elif,except,exec,finally,from,global,import,in,is,lambda,"+"nonlocal,not,or,pass,print,raise,try,with,yield,"+"False,True,None"];var RUBY_KEYWORDS=[FLOW_CONTROL_KEYWORDS,"alias,and,begin,case,class,"+"def,defined,elsif,end,ensure,false,in,module,next,nil,not,or,redo,"+"rescue,retry,self,super,then,true,undef,unless,until,when,yield,"+"BEGIN,END"];var SH_KEYWORDS=[FLOW_CONTROL_KEYWORDS,"case,done,elif,esac,eval,fi,"+"function,in,local,set,then,until"];var ALL_KEYWORDS=[CPP_KEYWORDS,CSHARP_KEYWORDS,JSCRIPT_KEYWORDS,PERL_KEYWORDS+
PYTHON_KEYWORDS,RUBY_KEYWORDS,SH_KEYWORDS];var C_TYPES=/^(DIR|FILE|vector|(de|priority_)?queue|list|stack|(const_)?iterator|(multi)?(set|map)|bitset|u?(int|float)\d*)\b/;var PR_STRING='str';var PR_KEYWORD='kwd';var PR_COMMENT='com';var PR_TYPE='typ';var PR_LITERAL='lit';var PR_PUNCTUATION='pun';var PR_PLAIN='pln';var PR_TAG='tag';var PR_DECLARATION='dec';var PR_SOURCE='src';var PR_ATTRIB_NAME='atn';var PR_ATTRIB_VALUE='atv';var PR_NOCODE='nocode';var REGEXP_PRECEDER_PATTERN='(?:^^\\.?|[+-]|[!=]=?=?|\\#|%=?|&&?=?|\\(|\\*=?|[+\\-]=|->|\\/=?|::?|<<?=?|>>?>?=?|,|;|\\?|@|\\[|~|{|\\^\\^?=?|\\|\\|?=?|break|case|continue|delete|do|else|finally|instanceof|return|throw|try|typeof)\\s*';function combinePrefixPatterns(regexs){var capturedGroupIndex=0;var needToFoldCase=false;var ignoreCase=false;for(var i=0,n=regexs.length;i<n;++i){var regex=regexs[i];if(regex.ignoreCase){ignoreCase=true;}else if(/[a-z]/i.test(regex.source.replace(/\\u[0-9a-f]{4}|\\x[0-9a-f]{2}|\\[^ux]/gi,''))){needToFoldCase=true;ignoreCase=false;break;}}
var escapeCharToCodeUnit={'b':8,'t':9,'n':0xa,'v':0xb,'f':0xc,'r':0xd};function decodeEscape(charsetPart){var cc0=charsetPart.charCodeAt(0);if(cc0!==92){return cc0;}
var c1=charsetPart.charAt(1);cc0=escapeCharToCodeUnit[c1];if(cc0){return cc0;}else if('0'<=c1&&c1<='7'){return parseInt(charsetPart.substring(1),8);}else if(c1==='u'||c1==='x'){return parseInt(charsetPart.substring(2),16);}else{return charsetPart.charCodeAt(1);}}
function encodeEscape(charCode){if(charCode<0x20){return(charCode<0x10?'\\x0':'\\x')+charCode.toString(16);}
var ch=String.fromCharCode(charCode);return(ch==='\\'||ch==='-'||ch===']'||ch==='^')?"\\"+ch:ch;}
function caseFoldCharset(charSet){var charsetParts=charSet.substring(1,charSet.length-1).match(new RegExp('\\\\u[0-9A-Fa-f]{4}'
+'|\\\\x[0-9A-Fa-f]{2}'
+'|\\\\[0-3][0-7]{0,2}'
+'|\\\\[0-7]{1,2}'
+'|\\\\[\\s\\S]'
+'|-'
+'|[^-\\\\]','g'));var ranges=[];var inverse=charsetParts[0]==='^';var out=['['];if(inverse){out.push('^');}
for(var i=inverse?1:0,n=charsetParts.length;i<n;++i){var p=charsetParts[i];if(/\\[bdsw]/i.test(p)){out.push(p);}else{var start=decodeEscape(p);var end;if(i+2<n&&'-'===charsetParts[i+1]){end=decodeEscape(charsetParts[i+2]);i+=2;}else{end=start;}
ranges.push([start,end]);if(!(end<65||start>122)){if(!(end<65||start>90)){ranges.push([Math.max(65,start)|32,Math.min(end,90)|32]);}
if(!(end<97||start>122)){ranges.push([Math.max(97,start)&~32,Math.min(end,122)&~32]);}}}}
ranges.sort(function(a,b){return(a[0]-b[0])||(b[1]-a[1]);});var consolidatedRanges=[];var lastRange=[];for(var i=0;i<ranges.length;++i){var range=ranges[i];if(range[0]<=lastRange[1]+1){lastRange[1]=Math.max(lastRange[1],range[1]);}else{consolidatedRanges.push(lastRange=range);}}
for(var i=0;i<consolidatedRanges.length;++i){var range=consolidatedRanges[i];out.push(encodeEscape(range[0]));if(range[1]>range[0]){if(range[1]+1>range[0]){out.push('-');}
out.push(encodeEscape(range[1]));}}
out.push(']');return out.join('');}
function allowAnywhereFoldCaseAndRenumberGroups(regex){var parts=regex.source.match(new RegExp('(?:'
+'\\[(?:[^\\x5C\\x5D]|\\\\[\\s\\S])*\\]'
+'|\\\\u[A-Fa-f0-9]{4}'
+'|\\\\x[A-Fa-f0-9]{2}'
+'|\\\\[0-9]+'
+'|\\\\[^ux0-9]'
+'|\\(\\?[:!=]'
+'|[\\(\\)\\^]'
+'|[^\\x5B\\x5C\\(\\)\\^]+'
+')','g'));var n=parts.length;var capturedGroups=[];for(var i=0,groupIndex=0;i<n;++i){var p=parts[i];if(p==='('){++groupIndex;}else if('\\'===p.charAt(0)){var decimalValue=+p.substring(1);if(decimalValue){if(decimalValue<=groupIndex){capturedGroups[decimalValue]=-1;}else{parts[i]=encodeEscape(decimalValue);}}}}
for(var i=1;i<capturedGroups.length;++i){if(-1===capturedGroups[i]){capturedGroups[i]=++capturedGroupIndex;}}
for(var i=0,groupIndex=0;i<n;++i){var p=parts[i];if(p==='('){++groupIndex;if(!capturedGroups[groupIndex]){parts[i]='(?:';}}else if('\\'===p.charAt(0)){var decimalValue=+p.substring(1);if(decimalValue&&decimalValue<=groupIndex){parts[i]='\\'+capturedGroups[decimalValue];}}}
for(var i=0;i<n;++i){if('^'===parts[i]&&'^'!==parts[i+1]){parts[i]='';}}
if(regex.ignoreCase&&needToFoldCase){for(var i=0;i<n;++i){var p=parts[i];var ch0=p.charAt(0);if(p.length>=2&&ch0==='['){parts[i]=caseFoldCharset(p);}else if(ch0!=='\\'){parts[i]=p.replace(/[a-zA-Z]/g,function(ch){var cc=ch.charCodeAt(0);return'['+String.fromCharCode(cc&~32,cc|32)+']';});}}}
return parts.join('');}
var rewritten=[];for(var i=0,n=regexs.length;i<n;++i){var regex=regexs[i];if(regex.global||regex.multiline){throw new Error(''+regex);}
rewritten.push('(?:'+allowAnywhereFoldCaseAndRenumberGroups(regex)+')');}
return new RegExp(rewritten.join('|'),ignoreCase?'gi':'g');}
function extractSourceSpans(node,isPreformatted){var nocode=/(?:^|\s)nocode(?:\s|$)/;var chunks=[];var length=0;var spans=[];var k=0;function walk(node){switch(node.nodeType){case 1:if(nocode.test(node.className)){return;}
for(var child=node.firstChild;child;child=child.nextSibling){walk(child);}
var nodeName=node.nodeName.toLowerCase();if('br'===nodeName||'li'===nodeName){chunks[k]='\n';spans[k<<1]=length++;spans[(k++<<1)|1]=node;}
break;case 3:case 4:var text=node.nodeValue;if(text.length){if(!isPreformatted){text=text.replace(/[ \t\r\n]+/g,' ');}else{text=text.replace(/\r\n?/g,'\n');}
chunks[k]=text;spans[k<<1]=length;length+=text.length;spans[(k++<<1)|1]=node;}
break;}}
walk(node);return{sourceCode:chunks.join('').replace(/\n$/,''),spans:spans};}
function appendDecorations(basePos,sourceCode,langHandler,out){if(!sourceCode){return;}
var job={sourceCode:sourceCode,basePos:basePos};langHandler(job);out.push.apply(out,job.decorations);}
var notWs=/\S/;function childContentWrapper(element){var wrapper=undefined;for(var c=element.firstChild;c;c=c.nextSibling){var type=c.nodeType;wrapper=(type===1)?(wrapper?element:c):(type===3)?(notWs.test(c.nodeValue)?element:wrapper):wrapper;}
return wrapper===element?undefined:wrapper;}
function createSimpleLexer(shortcutStylePatterns,fallthroughStylePatterns){var shortcuts={};var tokenizer;(function(){var allPatterns=shortcutStylePatterns.concat(fallthroughStylePatterns);var allRegexs=[];var regexKeys={};for(var i=0,n=allPatterns.length;i<n;++i){var patternParts=allPatterns[i];var shortcutChars=patternParts[3];if(shortcutChars){for(var c=shortcutChars.length;--c>=0;){shortcuts[shortcutChars.charAt(c)]=patternParts;}}
var regex=patternParts[1];var k=''+regex;if(!regexKeys.hasOwnProperty(k)){allRegexs.push(regex);regexKeys[k]=null;}}
allRegexs.push(/[\0-\uffff]/);tokenizer=combinePrefixPatterns(allRegexs);})();var nPatterns=fallthroughStylePatterns.length;var decorate=function(job){var sourceCode=job.sourceCode,basePos=job.basePos;var decorations=[basePos,PR_PLAIN];var pos=0;var tokens=sourceCode.match(tokenizer)||[];var styleCache={};for(var ti=0,nTokens=tokens.length;ti<nTokens;++ti){var token=tokens[ti];var style=styleCache[token];var match=void 0;var isEmbedded;if(typeof style==='string'){isEmbedded=false;}else{var patternParts=shortcuts[token.charAt(0)];if(patternParts){match=token.match(patternParts[1]);style=patternParts[0];}else{for(var i=0;i<nPatterns;++i){patternParts=fallthroughStylePatterns[i];match=token.match(patternParts[1]);if(match){style=patternParts[0];break;}}
if(!match){style=PR_PLAIN;}}
isEmbedded=style.length>=5&&'lang-'===style.substring(0,5);if(isEmbedded&&!(match&&typeof match[1]==='string')){isEmbedded=false;style=PR_SOURCE;}
if(!isEmbedded){styleCache[token]=style;}}
var tokenStart=pos;pos+=token.length;if(!isEmbedded){decorations.push(basePos+tokenStart,style);}else{var embeddedSource=match[1];var embeddedSourceStart=token.indexOf(embeddedSource);var embeddedSourceEnd=embeddedSourceStart+embeddedSource.length;if(match[2]){embeddedSourceEnd=token.length-match[2].length;embeddedSourceStart=embeddedSourceEnd-embeddedSource.length;}
var lang=style.substring(5);appendDecorations(basePos+tokenStart,token.substring(0,embeddedSourceStart),decorate,decorations);appendDecorations(basePos+tokenStart+embeddedSourceStart,embeddedSource,langHandlerForExtension(lang,embeddedSource),decorations);appendDecorations(basePos+tokenStart+embeddedSourceEnd,token.substring(embeddedSourceEnd),decorate,decorations);}}
job.decorations=decorations;};return decorate;}
function sourceDecorator(options){var shortcutStylePatterns=[],fallthroughStylePatterns=[];if(options['tripleQuotedStrings']){shortcutStylePatterns.push([PR_STRING,/^(?:\'\'\'(?:[^\'\\]|\\[\s\S]|\'{1,2}(?=[^\']))*(?:\'\'\'|$)|\"\"\"(?:[^\"\\]|\\[\s\S]|\"{1,2}(?=[^\"]))*(?:\"\"\"|$)|\'(?:[^\\\']|\\[\s\S])*(?:\'|$)|\"(?:[^\\\"]|\\[\s\S])*(?:\"|$))/,null,'\'"']);}else if(options['multiLineStrings']){shortcutStylePatterns.push([PR_STRING,/^(?:\'(?:[^\\\']|\\[\s\S])*(?:\'|$)|\"(?:[^\\\"]|\\[\s\S])*(?:\"|$)|\`(?:[^\\\`]|\\[\s\S])*(?:\`|$))/,null,'\'"`']);}else{shortcutStylePatterns.push([PR_STRING,/^(?:\'(?:[^\\\'\r\n]|\\.)*(?:\'|$)|\"(?:[^\\\"\r\n]|\\.)*(?:\"|$))/,null,'"\'']);}
if(options['verbatimStrings']){fallthroughStylePatterns.push([PR_STRING,/^@\"(?:[^\"]|\"\")*(?:\"|$)/,null]);}
var hc=options['hashComments'];if(hc){if(options['cStyleComments']){if(hc>1){shortcutStylePatterns.push([PR_COMMENT,/^#(?:##(?:[^#]|#(?!##))*(?:###|$)|.*)/,null,'#']);}else{shortcutStylePatterns.push([PR_COMMENT,/^#(?:(?:define|e(?:l|nd)if|else|error|ifn?def|include|line|pragma|undef|warning)\b|[^\r\n]*)/,null,'#']);}
fallthroughStylePatterns.push([PR_STRING,/^<(?:(?:(?:\.\.\/)*|\/?)(?:[\w-]+(?:\/[\w-]+)+)?[\w-]+\.h(?:h|pp|\+\+)?|[a-z]\w*)>/,null]);}else{shortcutStylePatterns.push([PR_COMMENT,/^#[^\r\n]*/,null,'#']);}}
if(options['cStyleComments']){fallthroughStylePatterns.push([PR_COMMENT,/^\/\/[^\r\n]*/,null]);fallthroughStylePatterns.push([PR_COMMENT,/^\/\*[\s\S]*?(?:\*\/|$)/,null]);}
if(options['regexLiterals']){var REGEX_LITERAL=('/(?=[^/*])'
+'(?:[^/\\x5B\\x5C]'
+'|\\x5C[\\s\\S]'
+'|\\x5B(?:[^\\x5C\\x5D]|\\x5C[\\s\\S])*(?:\\x5D|$))+'
+'/');fallthroughStylePatterns.push(['lang-regex',new RegExp('^'+REGEXP_PRECEDER_PATTERN+'('+REGEX_LITERAL+')')]);}
var types=options['types'];if(types){fallthroughStylePatterns.push([PR_TYPE,types]);}
var keywords=(""+options['keywords']).replace(/^ | $/g,'');if(keywords.length){fallthroughStylePatterns.push([PR_KEYWORD,new RegExp('^(?:'+keywords.replace(/[\s,]+/g,'|')+')\\b'),null]);}
shortcutStylePatterns.push([PR_PLAIN,/^\s+/,null,' \r\n\t\xA0']);var punctuation=/^.[^\s\w\.$@\'\"\`\/\\]*/;fallthroughStylePatterns.push([PR_LITERAL,/^@[a-z_$][a-z_$@0-9]*/i,null],[PR_TYPE,/^(?:[@_]?[A-Z]+[a-z][A-Za-z_$@0-9]*|\w+_t\b)/,null],[PR_PLAIN,/^[a-z_$][a-z_$@0-9]*/i,null],[PR_LITERAL,new RegExp('^(?:'
+'0x[a-f0-9]+'
+'|(?:\\d(?:_\\d+)*\\d*(?:\\.\\d*)?|\\.\\d\\+)'
+'(?:e[+\\-]?\\d+)?'
+')'
+'[a-z]*','i'),null,'0123456789'],[PR_PLAIN,/^\\[\s\S]?/,null],[PR_PUNCTUATION,punctuation,null]);return createSimpleLexer(shortcutStylePatterns,fallthroughStylePatterns);}
var decorateSource=sourceDecorator({'keywords':ALL_KEYWORDS,'hashComments':true,'cStyleComments':true,'multiLineStrings':true,'regexLiterals':true});function numberLines(node,opt_startLineNum,isPreformatted){var nocode=/(?:^|\s)nocode(?:\s|$)/;var lineBreak=/\r\n?|\n/;var document=node.ownerDocument;var li=document.createElement('li');while(node.firstChild){li.appendChild(node.firstChild);}
var listItems=[li];function walk(node){switch(node.nodeType){case 1:if(nocode.test(node.className)){break;}
if('br'===node.nodeName){breakAfter(node);if(node.parentNode){node.parentNode.removeChild(node);}}else{for(var child=node.firstChild;child;child=child.nextSibling){walk(child);}}
break;case 3:case 4:if(isPreformatted){var text=node.nodeValue;var match=text.match(lineBreak);if(match){var firstLine=text.substring(0,match.index);node.nodeValue=firstLine;var tail=text.substring(match.index+match[0].length);if(tail){var parent=node.parentNode;parent.insertBefore(document.createTextNode(tail),node.nextSibling);}
breakAfter(node);if(!firstLine){node.parentNode.removeChild(node);}}}
break;}}
function breakAfter(lineEndNode){while(!lineEndNode.nextSibling){lineEndNode=lineEndNode.parentNode;if(!lineEndNode){return;}}
function breakLeftOf(limit,copy){var rightSide=copy?limit.cloneNode(false):limit;var parent=limit.parentNode;if(parent){var parentClone=breakLeftOf(parent,1);var next=limit.nextSibling;parentClone.appendChild(rightSide);for(var sibling=next;sibling;sibling=next){next=sibling.nextSibling;parentClone.appendChild(sibling);}}
return rightSide;}
var copiedListItem=breakLeftOf(lineEndNode.nextSibling,0);for(var parent;(parent=copiedListItem.parentNode)&&parent.nodeType===1;){copiedListItem=parent;}
listItems.push(copiedListItem);}
for(var i=0;i<listItems.length;++i){walk(listItems[i]);}
if(opt_startLineNum===(opt_startLineNum|0)){listItems[0].setAttribute('value',opt_startLineNum);}
var ol=document.createElement('ol');ol.className='linenums';var offset=Math.max(0,((opt_startLineNum-1))|0)||0;for(var i=0,n=listItems.length;i<n;++i){li=listItems[i];li.className='L'+((i+offset)%10);if(!li.firstChild){li.appendChild(document.createTextNode('\xA0'));}
ol.appendChild(li);}
node.appendChild(ol);}
function recombineTagsAndDecorations(job){var isIE8OrEarlier=/\bMSIE\s(\d+)/.exec(navigator.userAgent);isIE8OrEarlier=isIE8OrEarlier&&+isIE8OrEarlier[1]<=8;var newlineRe=/\n/g;var source=job.sourceCode;var sourceLength=source.length;var sourceIndex=0;var spans=job.spans;var nSpans=spans.length;var spanIndex=0;var decorations=job.decorations;var nDecorations=decorations.length;var decorationIndex=0;decorations[nDecorations]=sourceLength;var decPos,i;for(i=decPos=0;i<nDecorations;){if(decorations[i]!==decorations[i+2]){decorations[decPos++]=decorations[i++];decorations[decPos++]=decorations[i++];}else{i+=2;}}
nDecorations=decPos;for(i=decPos=0;i<nDecorations;){var startPos=decorations[i];var startDec=decorations[i+1];var end=i+2;while(end+2<=nDecorations&&decorations[end+1]===startDec){end+=2;}
decorations[decPos++]=startPos;decorations[decPos++]=startDec;i=end;}
nDecorations=decorations.length=decPos;var sourceNode=job.sourceNode;var oldDisplay;if(sourceNode){oldDisplay=sourceNode.style.display;sourceNode.style.display='none';}
try{var decoration=null;while(spanIndex<nSpans){var spanStart=spans[spanIndex];var spanEnd=spans[spanIndex+2]||sourceLength;var decEnd=decorations[decorationIndex+2]||sourceLength;var end=Math.min(spanEnd,decEnd);var textNode=spans[spanIndex+1];var styledText;if(textNode.nodeType!==1&&(styledText=source.substring(sourceIndex,end))){if(isIE8OrEarlier){styledText=styledText.replace(newlineRe,'\r');}
textNode.nodeValue=styledText;var document=textNode.ownerDocument;var span=document.createElement('span');span.className=decorations[decorationIndex+1];var parentNode=textNode.parentNode;parentNode.replaceChild(span,textNode);span.appendChild(textNode);if(sourceIndex<spanEnd){spans[spanIndex+1]=textNode=document.createTextNode(source.substring(end,spanEnd));parentNode.insertBefore(textNode,span.nextSibling);}}
sourceIndex=end;if(sourceIndex>=spanEnd){spanIndex+=2;}
if(sourceIndex>=decEnd){decorationIndex+=2;}}}finally{if(sourceNode){sourceNode.style.display=oldDisplay;}}}
var langHandlerRegistry={};function registerLangHandler(handler,fileExtensions){for(var i=fileExtensions.length;--i>=0;){var ext=fileExtensions[i];if(!langHandlerRegistry.hasOwnProperty(ext)){langHandlerRegistry[ext]=handler;}else if(win['console']){console['warn']('cannot override language handler %s',ext);}}}
function langHandlerForExtension(extension,source){if(!(extension&&langHandlerRegistry.hasOwnProperty(extension))){extension=/^\s*</.test(source)?'default-markup':'default-code';}
return langHandlerRegistry[extension];}
registerLangHandler(decorateSource,['default-code']);registerLangHandler(createSimpleLexer([],[[PR_PLAIN,/^[^<?]+/],[PR_DECLARATION,/^<!\w[^>]*(?:>|$)/],[PR_COMMENT,/^<\!--[\s\S]*?(?:-\->|$)/],['lang-',/^<\?([\s\S]+?)(?:\?>|$)/],['lang-',/^<%([\s\S]+?)(?:%>|$)/],[PR_PUNCTUATION,/^(?:<[%?]|[%?]>)/],['lang-',/^<xmp\b[^>]*>([\s\S]+?)<\/xmp\b[^>]*>/i],['lang-js',/^<script\b[^>]*>([\s\S]*?)(<\/script\b[^>]*>)/i],['lang-css',/^<style\b[^>]*>([\s\S]*?)(<\/style\b[^>]*>)/i],['lang-in.tag',/^(<\/?[a-z][^<>]*>)/i]]),['default-markup','htm','html','mxml','xhtml','xml','xsl']);registerLangHandler(createSimpleLexer([[PR_PLAIN,/^[\s]+/,null,' \t\r\n'],[PR_ATTRIB_VALUE,/^(?:\"[^\"]*\"?|\'[^\']*\'?)/,null,'\"\'']],[[PR_TAG,/^^<\/?[a-z](?:[\w.:-]*\w)?|\/?>$/i],[PR_ATTRIB_NAME,/^(?!style[\s=]|on)[a-z](?:[\w:-]*\w)?/i],['lang-uq.val',/^=\s*([^>\'\"\s]*(?:[^>\'\"\s\/]|\/(?=\s)))/],[PR_PUNCTUATION,/^[=<>\/]+/],['lang-js',/^on\w+\s*=\s*\"([^\"]+)\"/i],['lang-js',/^on\w+\s*=\s*\'([^\']+)\'/i],['lang-js',/^on\w+\s*=\s*([^\"\'>\s]+)/i],['lang-css',/^style\s*=\s*\"([^\"]+)\"/i],['lang-css',/^style\s*=\s*\'([^\']+)\'/i],['lang-css',/^style\s*=\s*([^\"\'>\s]+)/i]]),['in.tag']);registerLangHandler(createSimpleLexer([],[[PR_ATTRIB_VALUE,/^[\s\S]+/]]),['uq.val']);registerLangHandler(sourceDecorator({'keywords':CPP_KEYWORDS,'hashComments':true,'cStyleComments':true,'types':C_TYPES}),['c','cc','cpp','cxx','cyc','m']);registerLangHandler(sourceDecorator({'keywords':'null,true,false'}),['json']);registerLangHandler(sourceDecorator({'keywords':CSHARP_KEYWORDS,'hashComments':true,'cStyleComments':true,'verbatimStrings':true,'types':C_TYPES}),['cs']);registerLangHandler(sourceDecorator({'keywords':JAVA_KEYWORDS,'cStyleComments':true}),['java']);registerLangHandler(sourceDecorator({'keywords':SH_KEYWORDS,'hashComments':true,'multiLineStrings':true}),['bsh','csh','sh']);registerLangHandler(sourceDecorator({'keywords':PYTHON_KEYWORDS,'hashComments':true,'multiLineStrings':true,'tripleQuotedStrings':true}),['cv','py']);registerLangHandler(sourceDecorator({'keywords':PERL_KEYWORDS,'hashComments':true,'multiLineStrings':true,'regexLiterals':true}),['perl','pl','pm']);registerLangHandler(sourceDecorator({'keywords':RUBY_KEYWORDS,'hashComments':true,'multiLineStrings':true,'regexLiterals':true}),['rb']);registerLangHandler(sourceDecorator({'keywords':JSCRIPT_KEYWORDS,'cStyleComments':true,'regexLiterals':true}),['js']);registerLangHandler(sourceDecorator({'keywords':COFFEE_KEYWORDS,'hashComments':3,'cStyleComments':true,'multilineStrings':true,'tripleQuotedStrings':true,'regexLiterals':true}),['coffee']);registerLangHandler(createSimpleLexer([],[[PR_STRING,/^[\s\S]+/]]),['regex']);function applyDecorator(job){var opt_langExtension=job.langExtension;try{var sourceAndSpans=extractSourceSpans(job.sourceNode,job.pre);var source=sourceAndSpans.sourceCode;job.sourceCode=source;job.spans=sourceAndSpans.spans;job.basePos=0;langHandlerForExtension(opt_langExtension,source)(job);recombineTagsAndDecorations(job);}catch(e){if(win['console']){console['log'](e&&e['stack']?e['stack']:e);}}}
function prettyPrintOne(sourceCodeHtml,opt_langExtension,opt_numberLines){var container=document.createElement('pre');container.innerHTML=sourceCodeHtml;if(opt_numberLines){numberLines(container,opt_numberLines,true);}
var job={langExtension:opt_langExtension,numberLines:opt_numberLines,sourceNode:container,pre:1};applyDecorator(job);return container.innerHTML;}
function prettyPrint(opt_whenDone){function byTagName(tn){return document.getElementsByTagName(tn);}
var codeSegments=[byTagName('pre'),byTagName('code'),byTagName('xmp')];var elements=[];for(var i=0;i<codeSegments.length;++i){for(var j=0,n=codeSegments[i].length;j<n;++j){elements.push(codeSegments[i][j]);}}
codeSegments=null;var clock=Date;if(!clock['now']){clock={'now':function(){return+(new Date);}};}
var k=0;var prettyPrintingJob;var langExtensionRe=/\blang(?:uage)?-([\w.]+)(?!\S)/;var prettyPrintRe=/\bprettyprint\b/;var prettyPrintedRe=/\bprettyprinted\b/;var preformattedTagNameRe=/pre|xmp/i;var codeRe=/^code$/i;var preCodeXmpRe=/^(?:pre|code|xmp)$/i;function doWork(){var endTime=(win['PR_SHOULD_USE_CONTINUATION']?clock['now']()+250:Infinity);for(;k<elements.length&&clock['now']()<endTime;k++){var cs=elements[k];var className=cs.className;if(prettyPrintRe.test(className)&&!prettyPrintedRe.test(className)){var nested=false;for(var p=cs.parentNode;p;p=p.parentNode){var tn=p.tagName;if(preCodeXmpRe.test(tn)&&p.className&&prettyPrintRe.test(p.className)){nested=true;break;}}
if(!nested){cs.className+=' prettyprinted';var langExtension=className.match(langExtensionRe);var wrapper;if(!langExtension&&(wrapper=childContentWrapper(cs))&&codeRe.test(wrapper.tagName)){langExtension=wrapper.className.match(langExtensionRe);}
if(langExtension){langExtension=langExtension[1];}
var preformatted;if(preformattedTagNameRe.test(cs.tagName)){preformatted=1;}else{var currentStyle=cs['currentStyle'];var whitespace=(currentStyle?currentStyle['whiteSpace']:(document.defaultView&&document.defaultView.getComputedStyle)?document.defaultView.getComputedStyle(cs,null).getPropertyValue('white-space'):0);preformatted=whitespace&&'pre'===whitespace.substring(0,3);}
var lineNums=cs.className.match(/\blinenums\b(?::(\d+))?/);lineNums=lineNums?lineNums[1]&&lineNums[1].length?+lineNums[1]:true:false;if(lineNums){numberLines(cs,lineNums,preformatted);}
prettyPrintingJob={langExtension:langExtension,sourceNode:cs,numberLines:lineNums,pre:preformatted};applyDecorator(prettyPrintingJob);}}}
if(k<elements.length){setTimeout(doWork,250);}else if(opt_whenDone){opt_whenDone();}}
doWork();}
var PR=win['PR']={'createSimpleLexer':createSimpleLexer,'registerLangHandler':registerLangHandler,'sourceDecorator':sourceDecorator,'PR_ATTRIB_NAME':PR_ATTRIB_NAME,'PR_ATTRIB_VALUE':PR_ATTRIB_VALUE,'PR_COMMENT':PR_COMMENT,'PR_DECLARATION':PR_DECLARATION,'PR_KEYWORD':PR_KEYWORD,'PR_LITERAL':PR_LITERAL,'PR_NOCODE':PR_NOCODE,'PR_PLAIN':PR_PLAIN,'PR_PUNCTUATION':PR_PUNCTUATION,'PR_SOURCE':PR_SOURCE,'PR_STRING':PR_STRING,'PR_TAG':PR_TAG,'PR_TYPE':PR_TYPE,'prettyPrintOne':win['prettyPrintOne']=prettyPrintOne,'prettyPrint':win['prettyPrint']=prettyPrint};if(typeof define==="function"&&define['amd']){define("google-code-prettify",[],function(){return PR;});}})();
;(function(window, document) {

  // Hide body until we're done fiddling with the DOM
  document.body.style.display = 'none';

  //////////////////////////////////////////////////////////////////////
  //
  // Shims for IE < 9
  //

  document.head = document.getElementsByTagName('head')[0];

  if (!('getElementsByClassName' in document)) {
    document.getElementsByClassName = function(name) {
      function getElementsByClassName(node, classname) {
        var a = [];
        var re = new RegExp('(^| )'+classname+'( |$)');
        var els = node.getElementsByTagName("*");
        for(var i=0,j=els.length; i<j; i++)
            if(re.test(els[i].className))a.push(els[i]);
        return a;
      }
      return getElementsByClassName(document.body, name);
    }
  }

  //////////////////////////////////////////////////////////////////////

  ///////////////
  // Initialize
  ///////////////
  neutriNote.init(document);

  ////////////////
  // Get raw data
  ////////////////
  var markdown = neutriNote.getData();

  ////////////////////////
  // Prepare the document
  ////////////////////////
  neutriNote.prepare();

  ///////////////////////
  // Additional setup
  ///////////////////////
  // Insert navbar if there's none
  var titleEl = document.getElementsByTagName('title')[0],
      scriptEls = document.getElementsByTagName('script'),
      navbarEl = document.getElementsByClassName('navbar')[0];

  var newNode = document.createElement('div');
  newNode.className = 'navbar navbar-fixed-top';
  if (!navbarEl && titleEl) {
      newNode.innerHTML = '<div class="navbar-inner"> <div class="container"> <div id="headline" class="brand"> </div> </div> </div>';
      document.body.insertBefore(newNode, document.body.firstChild);
      var title = titleEl.innerHTML;
      var headlineEl = document.getElementById('headline');
      if (headlineEl)
        headlineEl.innerHTML = title;
  }

  //////////////////////
  // Generate Markdown
  //////////////////////
  var converter = new Markdown.Converter();
  Markdown.Extra.init(converter, {extensions: "all"});
  var html = converter.makeHtml(markdown);

  // To allow reflow
  html = '<div id="nano_content">' + html + '</div>';

  ////////////////
  // Set content
  ////////////////
  neutriNote.setContent(html)

  // Prettify
  neutriNote.applyCodeStyle('code');

  prettyPrint();

  // Style tables
  neutriNote.applyTableStyle('table');

  // All done - show body
  document.body.style.display = '';

})(window, document);



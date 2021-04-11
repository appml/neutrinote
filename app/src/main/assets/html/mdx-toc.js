$(document).ready(function () {
    var toc = "<nav role='navigation'>";
    var newLine, el, title, link;

    $(".container h1, .container h2, .container h3, .container h4, .container h5, .container h6").each(function() {
        el = $(this);
        title = el.text();

        if (el.attr("id")) {
            link = "#" + el.attr("id");
            newLine = "<a href='" + link + "'>" + title + "</a>" + "<br />";

            if (el.is("h2"))
            newLine = "<span style='margin-left:1em' />" + newLine;
            else if (el.is("h3"))
            newLine = "<span style='margin-left:2em' />" + newLine;
            else if (el.is("h4"))
            newLine = "<span style='margin-left:3em' />" + newLine;
            else if (el.is("h5"))
            newLine = "<span style='margin-left:4em' />" + newLine;
            else if (el.is("h6"))
            newLine = "<span style='margin-left:5em' />" + newLine;

            toc += newLine;
        }
    });

    toc += "</nav>";
    $("#toc").replaceWith(toc);
});
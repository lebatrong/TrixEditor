var TE = {};

TE.editor = document.getElementById("editor");

// Initializations
TE.callback = function() {
    window.location.href = "te-callback://" + encodeURI(TE.getHtml());
}

TE.getHtml = function() {
    return TE.editor.innerHTML;
}

TE.getText = function() {
    return TE.editor.innerText;
}

TE.setHtml = function(contents) {
    TE.editor.innerHTML = decodeURIComponent(contents.replace(/\+/g, '%20'));
}
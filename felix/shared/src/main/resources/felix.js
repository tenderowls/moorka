var Felix = function () {
  "use strict";
  return {
    createElementAndSetId: function(tag, id) {
      var element = document.createElement(tag)
      element.id = id
      return element;
    },
    appendChildren: function (where, children) {
      var i, child, 
        childrenLen = children.length,
        fragment = document.createDocumentFragment();
      for (i = 0; i < childrenLen; i++) {
        child = children[i];
        if (typeof child === "string") {
          child = document.createTextNode(child);
        } 
        fragment.appendChild(child);
      }
      where.appendChild(fragment);
    }
  }
}();

Element.prototype.appendChildren = function(elements) {
  var fragment = document.createDocumentFragment(),
    i = 0,
    l = elements.length,
    el = null;

  for (i = 0; i < l; i++) {
    element = elements[i];
    fragment.appendChild(element);
  }
  
  this.appendChild(fragment);
};

EventTarget.prototype.addEventListenerWhichPreventDefault = function (eventType, listener) {
  this.addEventListener(eventType, function (event) {
    event.preventDefault();
    listener(event);
  });
}

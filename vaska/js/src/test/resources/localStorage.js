if (this.localStorage === undefined) {
  this.localStorage = new (function LocalStorage() {
    var storage = {};
    this.getItem = function(key) {
      var value = storage[key];
      if (value === undefined) return null;
      else return value;
    }
    this.setItem = function(key, value) {
      storage[key] = value;
    }
  })();
}  

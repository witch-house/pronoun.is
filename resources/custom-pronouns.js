(function () {
  'use strict';

  var urlRoot = 'http://pronoun.is/',
    urlParams = ['subject', 'object', 'possessive-determiner', 'possessive-pronoun', 'reflexive'];

  document.addEventListener('DOMContentLoaded', function () {
    // Only show the custom pronoun form if js is enabled and we've got a modern-ish browser
    document.body.classList.add('js');
    setup();
  }, false)

  var form,
    refers,
    urlLink,
    defaultPronouns = {},
    pronouns = {};

  function setup() {
    form = document.querySelector('.custom-pronoun form');
    refers = arrayFrom(form.querySelectorAll('[data-refer]'));
    urlLink = document.querySelector('.custom-pronoun .url');

    form.addEventListener('submit', function (event) {
      event.preventDefault();
    }, false);

    arrayFrom(form.querySelectorAll('[name]')).forEach(function (el) {
      defaultPronouns[el.name] = (el.getAttribute('placeholder') || '').trim().toLowerCase();
      pronouns[el.name] = el.value.trim().toLowerCase() || null;
    });

    form.addEventListener('input', handleFormInput, false);
    form.addEventListener('change', handleFormInput, false);
  }

  function handleFormInput(event) {
    var input = event.target,
      name = input.name,
      value = input.value.trim().toLowerCase() || null;

    pronouns[name] = value;
    updateRefers(name, value);
    update(pronouns);
  }

  function update(pronouns) {
    var sections = urlSections(urlParams, pronouns);
    if (sections.length >= urlParams.length) {
      var url = formatUrl(urlRoot, urlParams, pronouns);
      showURL(url);
      return;
    }

    if (sections.length >= 1) {
      checkDatabase(clone(pronouns));
    }

    hideURL();
  }

  var checkDatabase = debounce(300, function (pronouns) {
    var url = formatUrl('/', urlParams, pronouns);
    if (url === '/') return;

    getJSON(url, function (err, dbPronouns) {
      if (err) {
        // Ignore errors loading suggested pronouns from the server,
        // the user can just fill out the form manually
        return null;
      }

      dbPronouns.forEach(function (pronoun, i) {
        var name = urlParams[i];
        updatePlaceholder(name, pronoun);
        defaultPronouns[name] = pronoun;
      });

      showURL(formatUrl(urlRoot, urlParams, pronouns));
    });
  });

  // DOM mutation stuff
  function updateRefers(name, value) {
    refers
      .filter(function (el) {
        return el.getAttribute('data-refer') === name;
      })
      .forEach(function (el) {
        var pronoun = value || defaultPronouns[name] || '';
        el.textContent = el.getAttribute('data-capitalize') ? capitalize(pronoun) : pronoun;
      });
  }

  function updatePlaceholder(name, value) {
    form[name].setAttribute('placeholder', value);
  }

  function showURL(url) {
    urlLink.style.display = 'initial';
    urlLink.href = url;
    urlLink.textContent = url;
  }

  function hideURL() {
    urlLink.style.display = 'none';
  }

  // URL formatting stuff
  function urlSections(urlParams, data) {
    var sections = urlParams.map(function(param) {
      if (data[param]) {
        return encodeURIComponent(data[param]);
      }
    });

    return takeWhile(sections, function(param) {
      return param != null;
    });
  }

  function formatUrl(urlRoot, urlParams, data) {
    return urlRoot + urlSections(urlParams, data).join('/');
  }

  // Utils:
  function takeWhile(arr, predicate) {
    var newArr = [];

    while(predicate(arr[newArr.length])) {
      newArr.push(arr[newArr.length]);
    }

    return newArr;
  }

  function debounce(timeout, fn) {
    var id;
    return function() {
      var args = arguments;
      clearTimeout(id);
      id = setTimeout(function() {
        fn.apply(null, args);
      }, timeout);
    }
  }

  function capitalize(str) {
    if (str.length === 0) return str;

    var chars = str.split(''),
      first = chars[0].toUpperCase();

    chars[0] = first;
    return chars.join('');
  }

  function clone(obj) {
    var newObj = {};
    for (var key in obj) {
      if (obj.hasOwnProperty(key)) {
        newObj[key] = obj[key];
      }
    }
    return newObj;
  }

  function getJSON(url, callback) {
    var req = new XMLHttpRequest();

    req.addEventListener('load', function() {
      var json;
      try {
        json = JSON.parse(req.responseText);
      } catch(e) {
        callback(e);
        return;
      }
      if (req.status !== 200 || json.error) {
        callback(json, null);
      } else {
        callback(null, json);
      }
    }, false);

    req.addEventListener('error', function() {
      callback(req);
    }, false);

    req.open("GET", url);
    req.setRequestHeader('Accept', 'application/json');
    req.send();
  }

  function arrayFrom(arrayLike) {
    return [].slice.call(arrayLike);
  }
}());

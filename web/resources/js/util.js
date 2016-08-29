/*
 * @(#) util.js Algem Web App 1.4.0 26/08/16
 *
 * Copyright (c) 2015-2016 Musiques Tangentes. All Rights Reserved.
 *
 * This file is part of Algem Web App.
 * Algem Web App is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Algem Web App is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Algem Web App. If not, see <http://www.gnu.org/licenses/>.
 */
function logVars() {
  var l = arguments.length;
  if (l == 0) {return;}
  for (var i = 0 ;i < l; i++) {
    $.each(arguments[i], function (key, value) {
      console.log(key, value);
    });
  }
}

/**
 * Pad a number with 0.
 * If size is undefined, default padding length is 2.
 * Ex. : 1 -> 01 ; 12 -> 12 ; 0 -> 00
 * @param {Number} pad length
 * @returns {Number.prototype.pad.s|String}
 */
Number.prototype.pad = function (size) {
  var s = String(this);
  while (s.length < (size || 2)) {
    s = "0" + s;
  }
  return s;
}

/**
 * Returns a string representation of the date in argument.
 * Format matches French locale : dd-mm-yyyy
 * @param {Date} d
 * @returns {String}
 */
function dateFormatFR(d) {
  if (!d instanceof Date) {
    console.log("erreur date");
  }
  //var arg = first.toLocaleString().replace(/\//g, "-");
  var s = d.toISOString().slice(0, 10);//2016-06-12 yyyy-mm-dd
  var dd = s.slice(8, 10);
  var mm = s.slice(5, 7);
  var y = s.slice(0, 4);
  var sep = "-";
  return dd.concat(sep, mm, sep, y);
}

/**
 * Gets a string representation of a time in minutes : hh:mm.
 * @param {type} m time in minutes
 * @returns {String} a time-formatted string
 */
function getTimeFromMinutes(m) {
  return (m/60>>0).pad() + ":" + (m%60).pad();
}

/**
 * Gets first and last day of the week the date in argument is.
 * @param {type} d selected date
 * @returns {getCurrentWeekDates.utilAnonym$0}
 */
function getCurrentWeekDates(d) {
  var first = new Date(d.setDate(d.getDate() - d.getDay() + 1));
  first.setHours(0, -first.getTimezoneOffset(), 0, 0); //removing the timezone offset.
  var last = new Date(first);
  var last = new Date(last.setDate(last.getDate() + last.getDay() + 5));
  last.setHours(0, -last.getTimezoneOffset(), 0, 0);

  return {first: first, last: last}
}

/**
 * Gets first and last day of the current month.
 * @returns {getCurrentMonthDates.utilAnonym$1}
 */
function getCurrentMonthDates() {
  var now = new Date();
  var first = new Date(now.getFullYear(), now.getMonth(), 1);
  first.setHours(0, -first.getTimezoneOffset(), 0, 0); //removing the timezone offset.
  var last = new Date();
  var last = new Date(last.getFullYear(), last.getMonth() + 1, 0);
  last.setHours(0, -last.getTimezoneOffset(), 0, 0);

  return {first: first, last: last};
}

function getLocale() {
  return navigator.languages && navigator.languages[0] || // Chrome / Firefox
               navigator.language ||   // All browsers
               navigator.userLanguage; // IE <= 10
}

function toLocaleStringSupportsLocales() {
    try {
        new Date().toLocaleString("i");
    } catch (e) {
        return e.name === "RangeError";
    }
    return false;
}

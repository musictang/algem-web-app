/*
 * @(#) util.js Algem Web App 1.4.0 29/06/2016
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

function getTimeFromMinutes(m) {
  return (m/60>>0).pad() + ":" + (m%60).pad();
}
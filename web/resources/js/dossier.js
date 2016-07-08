/*
 * @(#) dossier.js Algem Web App 1.4.0 29/06/2016
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
function getFollowUp(urlPath, user, dateFrom, dateTo) {
//  var urlPath = $(el).attr("formaction");
  console.log(urlPath);
  $.get(urlPath, {userId: user, from: dateFrom, to: dateTo}, function (data) {
    if (typeof data === 'undefined' || !data.length) {
      console.log("no data");
      $("#follow-up-result tbody").empty(); // supprimer contenu
    } else {
      //console.log(data);
      var result = "";
      var total = 0;
      var indTitle = "Cliquez-moi pour éditer mon suivi individuel";
      var coTitle = "Cliquez-moi pour éditer le suivi collectif";
      $.each(data, function (index, value) {
        var d = new Date(value.date);
        var dateInfo = d.toLocaleString(getLocale(), {weekday: 'long'}) + " " + dateFormatFR(d);
        var timeInfo = value.start.hour.pad() + ":" + value.start.minute.pad() + "-" + value.end.hour.pad() + ":" + value.end.minute.pad();
        var ms = (value.start.hour * 60) + value.start.minute;
        var me = (value.end.hour * 60) + value.end.minute;
        var length = me - ms;
        var roomInfo = value.detail['room'].name;
        var courseInfo = value.detail['course'].name;
        var firstNameName = "";
        var noteInd = "";
        var noteCo = value.detail['note'].name || "";

        total += length;

        result += "<tr id=\""+value.id+"\">" // id planning
          + "<td>" + dateInfo + "</td>"
          + "<td>" + timeInfo + "</td>"
          + "<td>" + getTimeFromMinutes(length) + "</td>"
          + "<td>" + roomInfo + "</td>"
          + "<td>" + courseInfo + "</td><td>";
        if (value.collective) {
          result += "<a href=\"javascript:;\" class=\"expand\">Liste des élèves...</a><ul class=\"simple collapse\">";
        } else {
          result += "<ul class=\"simple\">";
        }
        for (var i = 0, len = value.ranges.length; i < len; i++) {
          firstNameName = value.ranges[i].person.firstName + " " + value.ranges[i].person.name;
          var nc = value.ranges[i].followUp.content;
          result += "<li id=\""+ value.ranges[i].id +"\"><a id=\"" + value.ranges[i].followUp.id + "\" href=\"javascript:;\" class=\"dlg\" title=\"" + indTitle + "\" accessKey=\"D\">" + firstNameName + "</a>";
          result += nc === null ? "</li>" : "<p>" + nc + "</p></li>";
        }
        result += "</ul>";
        result += "</td><td id=\"" + value.note + "\" class=\"dlg\" accessKey=\"C\">" + noteCo + "</td></tr>\n";
      });
      result += "<tr><th colspan=\"2\">Total</th><td colspan=\"5\"><b>" + getTimeFromMinutes(total) + "</b></td></tr>";
      $("#follow-up-result tbody").html(result);
    }
  }, "json");
}

function initFollowUpDialog(element) {
  $(element).dialog({
    modal: false,
    autoOpen: false,
    maxWidth: 320,
//    height: 500,
    buttons: {
      Abandonner: function () {
        $(this).dialog("close");
      },
      Enregistrer: function () {
        updateFollowUp($("#follow-up-form"));
        $(this).dialog("close");
      }
    }
  });
}

function showDialog(f) {
  var dlg = $("#follow-up-dlg");
  $(dlg).find("legend").html(f.name+ " : " + f.time);
//  console.log($(dlg).find("legend").html());
  $(dlg).dialog({title: f.course + " " + f.date}).dialog("open");
}

function FollowUpElement(id, date, time, course) {
  this.id = id;
  this.date = date;
  this.time = time;
  this.course = course;
}

function updateFollowUp(form) {
  var url = $(form).attr("action");
  var id = $(form).find("#noteId").val();
  var schedule = $(form).find("#scheduleId").val();
  var collective = $(form).find("#noteType").val();
  console.log(url);
  console.log(id);
  console.log(schedule);
  console.log(collective);
  $.post(url, form.serialize(), function (data) {
    console.log(data);
    if (data && data.success) {
      var content = $("#follow-content").val();
      console.log("id = " +id + ", content = " + content);
      console.log(collective);
      refreshFollowContent(id, data, content);
    } else {
      console.log("erreur !!!")
    }
  }, "json");
}

function refreshFollowContent(id, data, content) {
  if (data.collective) {
    console.log("c'est collectif")
    $("#" + id).text(content);
  } else {
    console.log(" id " + id + " c'est individuel")
    if (data.creation) {
      $("#" + id).closest("li").append("<p>" + content + "</p>");
    } else {
      $("#" + id).next().text(content);
    }
  }
}

function setWeekDates(firstDay, lastDay) {
  var from = $("#weekFrom");
  from.datepicker({changeMonth: true, changeYear: true, dateFormat: 'dd-mm-yy'}).datepicker('setDate', firstDay);
  from.datepicker("refresh");
  var to = $("#weekTo");
  to.datepicker({changeMonth: true, changeYear: true, dateFormat: 'dd-mm-yy'}).datepicker('setDate', lastDay);
}

function setWeekChange(url, idper) {
  var from = $("#weekFrom");
  var to = $("#weekTo");
  from.change(function () {
    console.log(this.value);
    var d = new Date(from.datepicker('getDate'));
    var wd = getCurrentWeekDates(d);
    getFollowUp(url, idper, dateFormatFR(wd.first), dateFormatFR(wd.last));
    from.datepicker('setDate', wd.first);
    to.datepicker('setDate', wd.last);
    from.blur();
  });
  
  to.change(function () {
    console.log(this.value);
    var d = new Date(to.datepicker('getDate'));
    var wd = getCurrentWeekDates(d);
    getFollowUp(url, idper, dateFormatFR(wd.first), dateFormatFR(wd.last));
    from.datepicker('setDate', wd.first);
    to.datepicker('setDate', wd.last);
    to.blur();
  });
}



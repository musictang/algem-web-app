/*
 * @(#)common.js    1.7.3 14/02/18
 *
 * Copyright (c) 2015-2018 Musiques Tangentes. All Rights Reserved.
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
 *
 */

/**
 * Global script for algem web module.
 * @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 * @version 1.7.3
 * @since 1.0.6 28/12/15
 */
function redirectLoginPage() {
  window.location.href = "login.html";
}

function setCommonEvents() {
  var delay = 500;
  // display ajax cursor
  jQuery.ajaxSetup({
    beforeSend: function () {$("#busy").show();},
    complete: function () {$("#busy").hide();},
//    xhrFields: { withCredentials: true },
    cache: false // IMPORTANT : IE,Edge fix
  });
  // redirect if session timeout
  $(document).ajaxError(function(event, jqXHR, ajaxSettings, thrownError) {
    if (jqXHR && (jqXHR.status == 403 || jqXHR.status == 405)) {
      console.log("jqXHR.status ",jqXHR.status);
      redirectLoginPage();
    }
  });

  //$(document).tooltip(); // default tooltip styling
  /*$(".labels").tooltip({
    content: function (callback) {
      callback($(this).prop('title').replace(/;/g, '<br />'));
    }
  });*/

  $('#bars').click(function () {
    $('#menu').show(delay);
  });

  $('.close-link').click(function () {
    $(this).parents('.closeable').hide(delay);
    return false;
  });

  $('#menu-close').click(function () {
    $('#menu').hide(delay);
    return false;
  });
  $('#help').click(function () {
    $(this).css('cursor', 'pointer');
    $('#help-panel').show(delay);
  });

  $('#info').click(function () {
    $(this).css('cursor', 'pointer');
    $('#info-panel').show(delay);
  });

  $('#menu-login').click(function () {
    $('#login-panel').show(delay);
  });

  $("#ajax-login-form").submit(function (event) {
    ajaxLogin($("#login-panel form"));
    event.preventDefault();
  });

  $("#signup-form, #reset-form").submit(function (event) {
    if ($(this).find("#password").val() !== $(this).find("#password2").val()) {
      $("#signup-error").text("Les mots de passe ne correspondent pas.")
      event.preventDefault();
    }
  });

  //postits display timeout (10 minutes)
  $(".postit").delay(7200000).hide(500);// !important : hide with argument
  $(".postit").click(function() {
    $(this).hide();
    var expire = new Date(new Date().getTime() + 1800000);//30 * 60 * 1000 (30 minutes)
    console.log(expire);
    if ($(this).hasClass("postit-private")) {
      document.cookie = "_PRS=true;expires="+expire;// PRIVATE_POSTIT_READ_STATUS
    } else {
      document.cookie = "PRS=true;expires="+expire;// PUBLIC_POSTIT_READ_STATUS
    }

  });

  var getHost = function () {
//    var port = (window.location.port == "8080") ? ":8443" : "";
    var port = window.location.port;
    return 'http://' + window.location.hostname + port;
//    return ((secure) ? 'https://' : 'http://') + window.location.hostname + port;
  };

  function ajaxLogin(form) {
    form.find(".error").hide();
    var urlPath = $("#login-panel form").attr('action');
    //jQuery.post( url [, data ] [, success ] [, dataType ] )
    $.post(
      urlPath,
      form.serialize(),
      function (data, textStatus) {
        var err = $("#login-panel p.error");
        var suc = $("#login-panel p.success");
        if (data.msg) {
          //console.log("ajax success");
          if (err.is(':visible')) {
            err.hide();
          }
          suc.html(data.msg);
          suc.show();
          //location.reload();
          window.location.href = "perso/home.html";
          //$("#login-panel").hide(2000);
        } else {
          //console.log("ajax error : " + data.status);
          if (suc.is(':visible')) {
            suc.hide();
          }
          err.html(data.status);
          err.show();
        }
      }, "json");
  }

}



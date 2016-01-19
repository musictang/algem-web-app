/*
 * @(#)common.js	1.0.6 28/12/15
 *
 * Copyright (c) 2013 Musiques Tangentes. All Rights Reserved.
 *
 * This file is part of Algem Agenda.
 * Algem Agenda is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Algem Agenda is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Algem Agenda. If not, see <http://www.gnu.org/licenses/>.
 *
 */

/*
 * Global script for algem agenda web module.
 @author <a href="mailto:jmg@musiques-tangentes.asso.fr">Jean-Marc Gobat</a>
 @version 1.0.6
 @since 1.0.6 28/12/15
 */

function setCommonEvents() {
  var delay = 500;
  jQuery.ajaxSetup({
    beforeSend: function() {
      console.log("beforeSend");
       $('body').addClass('busy');
    },
    complete: function() {
       $('body').removeClass('busy');
    }
});

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
    console.log("menu-login")
    $('#login-panel').show(delay);
  });

  $("#login-panel form input[type='button']").click(function () {
    console.log("click login button");
    performLogin($("#login-panel form"));
  });

  var getHost = function () {
//    var port = (window.location.port == "8080") ? ":8443" : "";
    var port = window.location.port;
    return 'http://' + window.location.hostname + port;
//    return ((secure) ? 'https://' : 'http://') + window.location.hostname + port;
  };
  
  function performLogin(form, errorMsg, successMsg) {
    form.find(".error").hide();
    var urlPath = $("#login-panel form").attr('action');
    console.log(urlPath);
    //jQuery.post( url [, data ] [, success ] [, dataType ] )
    $.post(
        urlPath,
        form.serialize(),
        function (data, textStatus) {
          var err = $("#login-panel p.error");
          var suc = $("#login-panel p.success");
          if (data.msg) {
            console.log("ajax success");
            if (err.is(':visible')) {
              err.hide();
            }
            suc.html(data.msg);
            suc.show();
            //$("#login-panel").hide(2000);
          } else {
            console.log("ajax error" + data.status);
            if (suc.is(':visible')) {
              suc.hide();
            }
            err.html(data.status);
            err.show();
          }
        },
        "json"
    );
  }

}


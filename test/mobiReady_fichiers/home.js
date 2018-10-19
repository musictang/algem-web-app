
var home = {
    isLandingPageTest: 0,
    apiBaseUrl: $("#homejs").attr('apiBaseUrl'),
    frontendBaseUrl: $("#homejs").attr('frontendBaseUrl'),
    test_url: "",
    init: function() {
        home.bindEvents();
        home.chkResetLink();

        $("footer").removeClass("initfooter");

        if ($("#aboutReady").length > 0) {
            // only run on about page, not home
            $("#signupBar").fadeIn();
            $("footer").addClass("initfooter");

            $('#accordion').on('hidden.bs.collapse', home.toggleChevron);
            $('#accordion').on('shown.bs.collapse', home.toggleChevron);
            $('.screenshotCarousel').cycle({
                fx: 'scrollUp'
            });
        }
    },
    bindEvents: function() {
        $(".exampleLink").click(function(e) {
            e.preventDefault();
            var href = $(this).attr("href");
            home.test_url = href;
            $("#test_url").val(href);
            $("#test_url").focus();
            //mobiReady.startTest();
        });
        $("#navAboutBtn").click(function(e) {
            e.preventDefault();
            home.navAbout();
        });
        $("#aAboutBtn").click(function(e) {
            e.preventDefault();
            home.navAbout();
        });
        $("#navLoginBtn").click(function(e) {
            e.preventDefault();
            home.navLogin();
        });
        $("#aLoginBtn").click(function(e) {
            e.preventDefault();
            home.navLogin();
        });
        $("#navSignupBtn").click(function(e) {
            e.preventDefault();
            home.navSignup();
        });
        $("#aSignupBtn").click(function(e) {
            e.preventDefault();
            home.navSignup();
        });
        $(".navSignupBtn").click(function(e) {
            e.preventDefault();
            home.navSignup();
        });
        $(".loginLink").click(function(e) {
            e.preventDefault();
            home.navLogin();
        });
        $("#forgottenLink").click(function(e) {
            e.preventDefault();
            home.navForgotten();
        });
        $("#LoginPassword").keypress(function(e) {
            if (e.which == 13) {
                home.doLogin();
            }
        });
        $("#loginBtn").click(function(e) {
            e.preventDefault();
            home.doLogin();
        });
        $("#log-in-tab").click(function(e) {
            e.preventDefault();
            home.navLogin();
        });
        $("#SignupPassword").keypress(function(e) {
            if (e.which == 13) {
                home.doSignup();
            }
        });
        $("#signupBtn").click(function(e) {
            e.preventDefault();
            home.doSignup();
        });
        $("#ForgottenEmail").keypress(function(e) {
            if (e.which == 13) {
                home.doForgotten();
            }
        });
        $("#forgottenBtn").click(function(e) {
            e.preventDefault();
            home.doForgotten();
        });
        $("#ResetPassword").keypress(function(e) {
            if (e.which == 13) {
                home.doReset();
            }
        });
        $("#resetBtn").click(function(e) {
            e.preventDefault();
            home.doReset();
        });
        $("#reportEmail").keypress(function(e) {
            if (e.which == 13) {
                home.doSendReport();
            }
        });
        $("#sendReportBtn").click(function(e) {
            e.preventDefault();
            home.doSendReport();
        });
        $("#landing_url").keypress(function(e) {
            if (e.which == 13) {
                home.doSubmitLandingPageModalForm();
            }
        });
        $("#landing_name").keypress(function(e) {
            if (e.which == 13) {
                home.doSubmitLandingPageModalForm();
            }
        });
        $("#landing_last_name").keypress(function(e) {
            if (e.which == 13) {
                home.doSubmitLandingPageModalForm();
            }
        });
        $("#landing_email").keypress(function(e) {
            if (e.which == 13) {
                home.doSubmitLandingPageModalForm();
            }
        });
        $("#landing_company").keypress(function(e) {
            if (e.which == 13) {
                home.doSubmitLandingPageModalForm();
            }
        });
        $("#freeReportBtn").click(function(e) {
            e.preventDefault();
            home.doSubmitLandingPageModalForm();
        });
        $('input:text, input:password').click(function(e) {
            home.clearPopovers();
        });
        $(window).on('beforeunload', function() {
            if (window.location.hash !== '' && window.location.origin == window.location.href) {
                localStorage.setItem('refresh', window.location.href);
                var x = logout();
                return '';
            }
        });
        function logout() {
            jQuery.ajax({
            });
            return '';
        }
        window.onload = function() {
            if (localStorage.getItem('refresh') == window.location.href) {
                localStorage.removeItem('refresh');
                history.replaceState({}, '', location.href.split('#')[0]);
                if (window.location.port == 8080)
                    window.location.replace('http://' + window.location.hostname + ':' + window.location.port);
                else
                    window.location.replace('http://' + window.location.hostname);
            }

            home.processLandingPage();
        }
    },
    chkResetLink: function() {
        home.clearPopovers();
        var hashKey = decodeURIComponent((new RegExp('[?|&]reset=' + '([^&;]+?)(&|#|;|$)').exec(location.search) || [, ""])[1].replace(/\+/g, '%20')) || null;
        if (hashKey) {
            $.post(home.frontendBaseUrl + "auth/reset-password", {'hash-key': hashKey})
                .done(function(data) {
                    // magic number!!!
                    if (data == 0) {
                        $("#resetFinish").modal();
                    } else {
                        // at least tell the usr that the url is bad :((
                        window.location.href = home.frontendBaseUrl;
                    }
                }
            );
        }
    },
    navForgotten: function() {
        home.clearPopovers();

        $("#login-bar").fadeOut("fast", function() {
            $("#forgotten-bar").fadeIn("fast", function() {
            });
        });


    },
    navAbout: function() {
        home.clearPopovers();
        window.location = home.frontendBaseUrl + "about";
    },
    navLogin: function() {
        home.clearPopovers();

        $("#resetFinish").modal('hide');
        $("#forgotten-bar").hide();
        $("#login-bar").fadeIn("fast", function() {
        });


        //set active tab
        $('#log-in-tab').tab('show');
        $('#credentials-modal').modal();
    },
    navSignup: function() {
        home.clearPopovers();

        $('#sign-up-tab').tab('show');
        $('#credentials-modal').modal();

    },
    scrollIntoView: function(id) {
        var s = $("#" + id).offset().top + $("#" + id).outerHeight(true) - $(window).height();
        if (s > 0) {
            $("html, body").animate({
                /*scrollTop: $("#" + id).offset().top - 100*/
                scrollTop: s
            });
        }
    },

    doReset: function() {
        var password = $("#ResetPassword").val();
        var resetSid = decodeURIComponent((new RegExp('[?|&]reset=' + '([^&;]+?)(&|#|;|$)').exec(location.search) || [, ""])[1].replace(/\+/g, '%20')) || null;

        if (password != "" && resetSid != "") {
            $.post(home.frontendBaseUrl + "auth/reset-password", {password: password, 'hash-key': resetSid})
                .done(function(data){
                    if (data == 1) {
                        $("#resetFinish").fadeOut("fast", function() {
                            home.navLogin();
                        });
                    } else {
                        $("#resetBtn").popover("destroy");
                        $("#resetBtn").popover({"content": "Reset failed, please retry", "placement": "top"});
                        $("#resetBtn").popover("show");
                    }
                }
            );
        } else {
            $("#resetBtn").popover("destroy");
            $("#resetBtn").popover({"content": "Fill in new password, then try again", "placement": "top"});
            $("#resetBtn").popover("show");
        }
    },
    doForgotten: function() {
        var email = $("#ForgottenEmail").val();

        if (email == "") {
            $("#ForgottenEmail").popover("destroy");
            $("#ForgottenEmail").popover({"content": "Email is required.", "placement": "top"});
            $("#ForgottenEmail").popover("show");
            return;
        }
        if (!home.validateEmail(email)) {
            $("#ForgottenEmail").popover("destroy");
            $("#ForgottenEmail").popover({"content": "Enter a valid email address.", "placement": "top"});
            $("#ForgottenEmail").popover("show");
            return;
        }

        $.post(home.frontendBaseUrl + "auth/reset-password", {email: email})
            .done(function(data) {
                if (data == 1) {
                    $("#forgottenBtn").popover("destroy");
                    $("#forgottenBtn").popover({"content": "Reset email has been sent.", "placement": "top"});
                    $("#forgottenBtn").popover("show");
                } else {
                    $("#forgottenBtn").popover("destroy");
                    $("#forgottenBtn").popover({"content": "E-mail address is not registered, Please retry.", "placement": "top"});
                    $("#forgottenBtn").popover("show");
                }
            })
            .fail(function() {
                $("#forgottenBtn").popover("destroy");
                $("#forgottenBtn").popover({"content": "Reset failed, please retry", "placement": "top"});
                $("#forgottenBtn").popover("show");
            });
    },
    doSignup: function() {
        var email = $("#SignupEmail").val();
        var password = $("#SignupPassword").val();
        if (email == "") {
            $("#SignupEmail").popover("destroy");
            $("#SignupEmail").popover({"content": "Email is required.", "placement": "top"});
            $("#SignupEmail").popover("show");
            return;
        }
        if (!home.validateEmail(email)) {
            $("#SignupEmail").popover("destroy");
            $("#SignupEmail").popover({"content": "Enter a valid email address.", "placement": "top"});
            $("#SignupEmail").popover("show");
            return;
        }
        if (password == "") {
            $("#SignupPassword").popover("destroy");
            $("#SignupPassword").popover({"content": "Password is required.", "placement": "top"});
            $("#SignupPassword").popover("show");
            return;
        }
        home._signup(email, password, "#signupBtn");
    },
    _signup: function(email, password, btn) {
        $.post(home.frontendBaseUrl + "auth/signup", {email: email, password: password})
            .done(function(data) {
                if (data == "1") {
                    $.post(home.frontendBaseUrl + "salesforce", {
                        email: email,
                        last_name: email.split('@')[0],
                        company: email.split('@')[1],
                        URL: "",
                        method: "1"
                    }).done(function() {
                        window.location = home.frontendBaseUrl + "dashboard#new";
                    });
                } else if (data == "-1") {
                    //duplicate email
                    $(btn).popover("destroy");
                    $(btn).popover({"content": "This email address already has an account, please login instead", "placement": "top"});
                    $(btn).popover("show");
                } else {
                    $(btn).popover("destroy");
                    $(btn).popover({"content": "Signup failed, please retry", "placement": "top"});
                    $(btn).popover("show");
                }
            }
        );
    },
    doSendReport: function() {

        var sid = window.location.hash.substring(1, window.location.hash.length);
        var email = $("#reportEmail").val();
        if (email == "") {
            $("#reportEmail").popover("destroy");
            $("#reportEmail").popover({"content": "Email is required.", "placement": "top"});
            $("#reportEmail").popover("show");
            $("#sendReportWaiter").fadeOut();
            return false;
        }
        if (!home.validateEmail(email)) {
            $("#reportEmail").popover("destroy");
            $("#reportEmail").popover({"content": "Enter a valid email address.", "placement": "top"});
            $("#reportEmail").popover("show");
            $("#sendReportWaiter").fadeOut();
            return false;
        }

        $("#sendReportWaiter").fadeIn();
        $.post(home.frontendBaseUrl + "test/send-pdf-report", {
            testid: sid,
            email: email,
            url: home.test_url
//            URL: $("#test_url").val()
        }).done(function(data) {
            //if it did not come from landing page (avoid duplications)
            $("#sendReportWaiter h3").text("Your report was sent.");
            $("#sendReportWaiter").fadeOut(3000);

            if (home.isLandingPageTest == 0) {
                $.post(home.frontendBaseUrl + "salesforce", {
                    email: email,
                    last_name: email.split('@')[0],
                    company: email.split('@')[1],
                    URL: home.test_url,
//                    URL: $("#test_url").val(),
                    method: "2"
                }).done(function() {
                });
            }
            home.isLandingPageTest = 0;
        });

    },
    validateEmail: function(email) {
        var filter = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/igm;
        if (filter.test(email)) {
            return true;
        }
        else {
            return false;
        }
    },
    clearPopovers: function() {
        $("#ResetPassword").popover("destroy");
        $("#resetBtn").popover("destroy");
        $("#forgottenEmail").popover("destroy");
        $("#forgottenPassword").popover("destroy");
        $("#forgottenBtn").popover("destroy");
        $("#signupBtn").popover("destroy");
        $("#SignupEmail").popover("destroy");
        $("#SignupPassword").popover("destroy");
        $("#SignupEmailTwo").popover("destroy");
        $("#SignupPasswordTwo").popover("destroy");
        $("#SignupEmailThree").popover("destroy");
        $("#SignupPasswordThree").popover("destroy");
        $("#LoginEmail").popover("destroy");
        $("#LoginPassword").popover("destroy");
        $("#loginBtn").popover("destroy");
        $("#reportEmail").popover("destroy");
    },
    doLogin: function() {
        var email = $("#LoginEmail").val();
        var password = $("#LoginPassword").val();

        if (email == "") {
            $("#LoginEmail").popover("destroy");
            $("#LoginEmail").popover({"content": "Email is required.", "placement": "top"});
            $("#LoginEmail").popover("show");
            return;
        }
        if (password == "") {
            $("#LoginPassword").popover("destroy");
            $("#LoginPassword").popover({"content": "Password is required.", "placement": "top"});
            $("#LoginPassword").popover("show");
            return;
        }

        $.post(home.frontendBaseUrl + "auth/signin", {email: email, password: password})
            .done(function(data) {
                if (data == "1") {
                    location = 'dashboard';
                    //$("#formLogin").submit();
                } else {
                    $("#loginBtn").popover("destroy");
                    $("#loginBtn").popover({"content": "Login failed, please retry", "placement": "top"});
                    $("#loginBtn").popover("show");
                }
            }
        );
    },
    doSubmitLandingPageModalForm: function() {
        //validate input
        var url = $("#landing_url").val();
        var name = $("#landing_name").val();
        var company = $("#landing_company").val();
        var email = $("#landing_email").val();
        var last_name = $("#landing_last_name").val();

        if (url == "") {
            $("#landing_url").popover("destroy");
            $("#landing_url").popover({"content": "Website is required.", "placement": "top"});
            $("#landing_url").popover("show");
            return false;
        }
        if (name == "") {
            $("#landing_name").popover("destroy");
            $("#landing_name").popover({"content": "Name is required.", "placement": "top"});
            $("#landing_name").popover("show");
            return false;
        }
        if (company == "") {
            $("#landing_company").popover("destroy");
            $("#landing_company").popover({"content": "Company name is required.", "placement": "top"});
            $("#landing_company").popover("show");
            return false;
        }
        if (email == "") {
            $("#landing_email").popover("destroy");
            $("#landing_email").popover({"content": "Email is required.", "placement": "top"});
            $("#landing_email").popover("show");
            return false;
        }
        if (!home.validateEmail(email)) {
            $("#landing_email").popover("destroy");
            $("#landing_email").popover({"content": "Enter a valid email address.", "placement": "top"});
            $("#landing_email").popover("show");
            return false;
        }
        if (last_name == "") {
            $("#landing_last_name").popover("destroy");
            $("#landing_last_name").popover({"content": "Last name is required.", "placement": "top"});
            $("#landing_last_name").popover("show");
            return false;
        }

        home._submitLandingPageModalForm();
    },
    _submitLandingPageModalForm: function() {

        //(first name, last name, email, company, phone, url)
        $.post(home.frontendBaseUrl + "salesforce", {
            URL: $("#landing_url").val(),
            first_name: $("#landing_name").val(),
            company: $("#landing_company").val(),
            email: $("#landing_email").val(),
            last_name: $("#landing_last_name").val(),
            phone: "",
            method: "3"
        }).done(function() {
        });

        //close modal
        $('#landingPageModal').modal('hide');
        //start test
        //set global var to send pdf report on test finished
        home.isLandingPageTest = 1;
        $("#test_url").val($("#landing_url").val());
        home.test_url = $("#landing_url").val();
        $("#reportEmail").val($("#landing_email").val());
        $("#go").click();

        //send pdf report on test finished
        var callback = setInterval(function() {
            if (home.isLandingPageTest == 2) {
                home.doSendReport();
                clearInterval(callback);
            }
        }, 1000);
    },
    processLandingPage: function() {
        var isNotDeepLink = window.location.hash == "";
        var isLandingPage = home.getParameterByName(window.location.href, "landing");
        if (isNotDeepLink && isLandingPage != null && isLandingPage == "1") {
            $('#landingPageModal').modal();
        }
    },
    getParameterByName: function(url, name) {
        name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
        var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
                results = regex.exec(url);
        return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
    },
    toggleChevron: function(e) {
        $(e.target)
                .prev('.panel-heading')
                .find("i.indicator")
                .toggleClass('accordion-icon-down accordion-icon-up');
    }
};

$(document).ready(function() {
    home.init();
});






d = {
    i: function () {
        var inspector = function (obj, ident) {
            var typ = typeof(obj), oTyp = 'DICTIONARY {\n', oBrac = '}';
            if(typ === 'object') {
                if(obj) {
                    if(typeof(obj.length) === 'number' && !(obj.propertyIsEnumerable('length')) &&
                        typeof(obj.splice) === 'function') {
                        oTyp = 'LIST [\n';
                        oBrac = ']';
                    }
                }
                else
                    typ = 'null';
            }
            switch(typ) {
                case 'null':      return 'null';
                case 'number':    return 'number '+obj;
                case 'string':    return 'string "'+obj+'"';
                case 'boolean':   return 'boolean '+obj;
                case 'undefined': return 'undefined';
                case 'function':  return 'function';
                case 'xml':       return 'xml';
                case 'object':
                    var ident2 = ident+'        ';
                    var strA = [];
                    jQuery.each(obj, function(k, v) { strA.push(ident2+k+': '+inspector(v, ident2)); });
                    return oTyp+strA.join(',\n')+'\n'+ident+oBrac;
            }
        };
        var text = '';
        var i, l=arguments.length;
        for(i=0; i<l; ++i)
            text += inspector(arguments[i], '')+"\n";
        alert(text);
    }
};
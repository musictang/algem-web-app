$(document).ready(function() {
    mobiReady.init();
});

var mobiReady = {

    DEBUG: true,
    PING_RESULTS_INTERVAL_MS: 2400,
    PING_RESULTS_CUTOFF: 200,

    sid: '',

    init: function () {
        var js = $('#readyjs');

        mobiReady.API_BASE_URL = js.attr('apiBaseUrl');
        mobiReady.API_URL = js.attr('apiBaseUrl') + 'api/v1/';
        mobiReady.BASE_URL = js.attr('frontendBaseUrl');

        $('#go').click(function (e) {
            e.preventDefault();
            mobiReady.startTest();
        });

        $('#test_url').keypress(function (e) {
            if (e.which === 13) {
                e.preventDefault();
                mobiReady.startTest();
            }
        });

        // if hash = testid then load result
        if (!mobiReady.loadResult()) {
            var url = this.getUrlParam('t');
            if (url) {
                history.replaceState(
                    {},
                    'mobiReady',
                    typeof location.origin === 'undefined' ? (location.origin = location.protocol + '//' + location.host) : location.origin
                );
                $('#test_url').val(url);
                mobiReady.startTest();
            }
        }
    },

    getUrlParam: function (key) {
        var a = window.location.search.substr(1).split('&');
        if (a == "") {
            return null;
        }
        var b = {};
        for (var i = 0; i < a.length; ++i) {
            var p=a[i].split('=', 2);
            if (p.length == 1) {
                b[p[0]] = '';
            } else {
                b[p[0]] = decodeURIComponent(p[1].replace(/\+/g, " "));
            }
        }
        return b[key]? b[key]: null;
    },

    loadResult: function() {
        var testId = $.trim(window.location.hash);
        if (testId.length > 5) {
            results.clean();
            mobiReady.sid = testId.substr(1);
            this.pingResults(function (res) {
                var url = res.summary.url;
                $('#test_url').val(url);

                if (typeof dash !== "undefined") {
                    $(".navbar-fixed-bottom").removeClass("navbar-fixed-bottom");
                    $(".sidebar .active").removeClass();
                    $("#navbar-results").addClass('active');

                    $('#results-pre-footer, #testUrlForm, #standard-footer').hide();
                    $("#results-footer").show();

                    dash.test_url = res.summary.url;
                }

                if (typeof home !== "undefined") {
                    home.test_url = res.summary.url;
                    $('#results-pre-footer').show();
                }

                $('#homeOuterContainer1, #homeOuterContainerMainImg, #homeOuterContainer2').hide();

            });
            return true;
        }
        return false;
    },

    startTest: function() {
        this.pingResultsCalls = 0;
        this.pingRequestFails = 0;

        var urlBox = $('#test_url'),
            url = $.trim(urlBox.val().replace(/^\s+|\s+$/g, ''));

        urlBox.popover('destroy');
        $(".main").css('padding-bottom', '');

        if (!mobiReady.validateUrl(url)) {
            return mobiReady.popMsg(urlBox, 'Please enter a valid webpage address and try again.');
        }
        if (url.indexOf('http') !== 0) {
            url = 'http://' + url;
        }

        // make domain lowercase
        url = url.split('://');
        url[1] = url[1].split('/');
        url[1][0] = url[1][0].toLowerCase();
        url[1] = url[1].join('/');
        url = url.join('://');

        urlBox.val(url);

        if (mobiReady.sid !== '') {
            return mobiReady.popMsg(urlBox, 'Test in progress, to cancel please refresh the page.');
        }

        $('#go').attr('disabled', true);
        $('.results').fadeOut();

        results.clean();

        $.ajax({
            url: this.BASE_URL + 'test/start',
            cache: false,
            type: 'get',
            data: {
                url: url
            },
            success: function (res) {
                $('#go').attr('disabled', false);

                if (res.atestid) {
                    window.location = mobiReady.BASE_URL + '/#' + res.atestid;
                    return;
                }

                if (!res.testid) {
                    return mobiReady.tryAgainMsg(urlBox);
                }

                mobiReady.sid = res.testid;

                if (typeof home !== 'undefined') {
                    home.test_url = url;
                    $('#results-pre-footer').show();
                }
                if (typeof dash !== 'undefined') {
                    dash.test_url = url;
                    $('#results-pre-footer, #testUrlForm, #standard-footer').hide();

                    $("#results-footer").show();
                    $(".navbar-fixed-bottom").removeClass("navbar-fixed-bottom");
                }

                $('.navbar-lower').affix({
                    offset: {top: 50}
                });

                $('#homeOuterContainer1, #homeOuterContainerMainImg, #homeOuterContainer2, .weight, #result-rank, #result-summary, #result-page-weight, #techbreakdown').hide();

                prism.load(url);

                $('#visualization').fadeIn();

                $('#navbar-visualization-link').click(function(e) {
                    e.preventDefault();
                    mobiReady.navigateToElement('visualization');
                });

                $('footer').removeClass('initfooter');

                setTimeout(function () { mobiReady.pingResults() }, mobiReady.PING_RESULTS_INTERVAL_MS);
            },
            error: function (xhr) {
                if (xhr.status == 415) {
                    mobiReady.notTestableMsg(urlBox);
                } else {
                    mobiReady.tryAgainMsg(urlBox);
                }
                $('#go').attr('disabled', false);
            }
        });
    },

    pingResults: function (fn) {
        var urlBox = $('#test_url');
        $.ajax({
            url: this.API_URL + 'test/page/result',
            cache: false,
            type: 'get',
            data: {
                testid: mobiReady.sid
            },
            success: function (res) {
                if (!results.validate(res)) {
                    // try 3 more times then give up
                    if (mobiReady.pingRequestFails++ < 3) {
                        setTimeout(function () { mobiReady.pingResults() }, mobiReady.PING_RESULTS_INTERVAL_MS);
                        return;
                    }
                    mobiReady.cancelTest();
                    return mobiReady.tryAgainMsg(urlBox);
                }

                if (res.summary.invalid) {
                    mobiReady.cancelTest();
                    return mobiReady.notTestableMsg(urlBox);
                }

                // true = completed, false = do another ping
                if (!results.set(res)) {
                    if (mobiReady.PING_RESULTS_CUTOFF > mobiReady.pingResultsCalls++) {
                        setTimeout(function () { mobiReady.pingResults() }, mobiReady.PING_RESULTS_INTERVAL_MS);
                    }
                }

                if (typeof fn === 'function') {
                    fn(res);
                }
            },
            error: function () {
                mobiReady.tryAgainMsg(urlBox);
            }
        });
    },

    cancelTest: function () {
        $('#homeOuterContainer1, #homeOuterContainerMainImg, #homeOuterContainer2').show();
        $('#visualization').hide();
        mobiReady.sid = '';
        $('#go').attr('disabled', false);
    },

    validateUrl: function (url) {
        if (url.length === 0) {
            return false;
        }
        return /^(http|https):\/\/[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.*)?$/i.test(url) ||
            /^[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.*)?$/i.test(url);
    },

    popMsg: function (e, msg) {
        $(".main").css("padding-bottom", "80px");
        e.popover({content: msg, placement: "bottom", html: true});
        e.popover("show");
    },

    /* show an appropriate to user when server could not initiate tests. */
    tryAgainMsg: function (e) {
        this.popMsg(e, 'We regret we are unable to process your request at present; please try again shortly.');
    },

    notTestableMsg: function (e) {
        this.popMsg(e, "We regret that mobiReady is unable to test this page. Please see <a target='_blank' href='/about#faq-not-testable'>FAQ</a> for more information");
    },

    navigateToElement: function (id) {
        $("html, body").animate({ scrollTop: $("#" + id).offset().top }, 1000);
    },

    getParameterByName: function (url, name) {
        name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
        var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
            results = regex.exec(url);
        return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
    },

    formatPdfReport: function() {
        $('#send-report-title, #send-report-container, #results-pre-footer, #breakdown-details-howtofix-button').remove();
        $('#progress-bar-container, #breakdown-details').show();
        $('#breakdownList').find('li')
            .css('font-size', '16px')
            .css('font-weight', '400');

        var html = '<ul>';
        for (var i = 0, l = results.tests.length; i<l; i++) {
            var test = results.tests[i];
            if (test.failure) {
                html += '<li><h3 class="pdf-header-' + test.type + '">' + test.definition.name + '</h3>';
                var errors = test.failure.split('\r\n');
                for (var j = 0, m = errors.length; j < m; j++) {
                    var msg = encode.html(errors[j]);
                    if (msg) {
                        html += '<p style="text-align:left;">' + (msg.indexOf(" : \n") > -1 ? (msg.replace(" : \n", '<code>') + '</code>') : msg.replace('\n', '<br/>')) + '</p>';
                    }
                }
                html += '</li>';
            }
        }

        $('#testdetails-container').html(html + '</ul>');
    },

    showBreakdownDetails: function (e) {
        var test = results.tests[$(e.currentTarget).attr('tid')],
            btnStr = 'How to fix it',
            title = test.definition.name;

        //reset
        $('#breakdownList').find('li')
            .css('font-size', '16px')
            .css('font-weight', 400);

        var bdd = $('#breakdown-details'),
            bddh = $('#breakdown-details-header'),
            tdh =  $('#testDetailsHeader'),
            tdhi = tdh.find('img'),
            tdhs = tdh.find('span');

        bddh.css('text-transform', '')
            .text(title);

        $(e.currentTarget).parent().css("font-size", "20px");
        $(e.currentTarget).parent().css("font-weight", "bold");

        if ($(e.currentTarget).attr("type") == "pass") {
            btnStr = 'More Info';
            bdd.html('<p>' + test.definition.description + '</p>');
            bddh.css("color", "#1fa89b")
                .css('text-transform', 'capitalize');
            tdhi.attr('src', 'img/pass-icon.png');
            tdhs.text('PASSED');

        } else if ($(e.currentTarget).attr("type") == "fail") {
            bddh.css("color", "#e06264");
            tdhi.attr('src',  mobiReady.BASE_URL + 'img/major-error-icon.png');
            tdhs.text('MAJOR FAIL');

        } else { //warn
            bddh.css("color", "#faa525");
            tdhi.attr('src', 'img/minor-error-icon.png');
            tdhs.text('MINOR FAIL');
        }

        $("#breakdown-details-howtofix-button").html(
            '<a target="_blank" href="' + test.definition.link + '"><input class="how-to-fix-btn" type="button" value="' + btnStr + '"/></a>'
        );

        var failure = '',
            errors = test.failure.split('\r\n');

        for (var i = 0, l = errors.length; i < l; i++) {
            var msg = encode.html(errors[i]);
            if (msg) {
                failure += '<li><span>' + (msg.indexOf(" : \n") > -1 ? (msg.replace(" : \n", '<code>') + '</code>') : msg.replace('\n', '<br/>')) + '</span></li>';
            }
        }
        bdd.html('<p>' + test.definition.description + '</p><ol>' + failure + '</ol>').show();

        var bg;
        if ($(e.currentTarget).attr("type") == "pass") {
            bg = "url('img/pass-indicator.png')";
        } else if ($(e.currentTarget).attr("type") == "fail") {
            bg = "url('" + mobiReady.BASE_URL + "img/major-fail-indicator.png')";
        } else {
            bg = "url('" + mobiReady.BASE_URL + "img/minor-fail-indicator.png')";
        }
        bdd.find('ol').find('li').css('background-image', bg);
    }
};


var encode = {

    html: function (string) {
        return String(string).replace(/[&<>"'\/]/g, function (s) {
            var map = {
                "&": "&amp;",
                "<": "&lt;",
                ">": "&gt;",
                '"': '&quot;',
                "'": '&#39;',
                "/": '&#x2F;'
            };
            return map[s];
        });
    }
};


var prism = {
    COLLECT_WEIGHTS: 0,

    load: function (url) {
        this.id = null;
        if (this.getWeightsTimeout) {
            clearTimeout(this.getWeightsTimeout);
        }
        this.loadFrames(encodeURIComponent(url));
    },

    loadFrames: function (url) {
        $.get(mobiReady.BASE_URL + '/test/prism/add?&i=' + this.COLLECT_WEIGHTS + '&testid=' + mobiReady.sid, function (res) {
            prism.id = parseInt(res, 10);
            if (isNaN(prism.id)) {
                return;
            }
            for (var i = 0; i < TestDevices.length; i++) {
                prism.loadFrame(url, i);
            }
        });
    },

    loadFrame: function (url, i) {
        var device = TestDevices[i];

        var proxyUrl = mobiReady.API_URL + 'prism/proxy' +
            '?prismid=' + prism.id +
            '&testid=' + mobiReady.sid +
            '&url=' + url +
            '&i=' + this.COLLECT_WEIGHTS +
            '&device=' + i;

        $('#prism-frame' + i)
            .attr('src', proxyUrl)
            .css({height: device.height + 'px', width: device.width + 'px'});
    }
};

var results = {

    clean: function () {
        this.completed = false;
        this.pageTestsCompleted = false;
        this.weightsCompleted = false;
        this.weightsCompletedDevices = [];
    },

    getTestTitle : function (name) {
        return name
            .toLowerCase().replace(/_/g, ' ')
            .replace(/javascript/g, "JavaScript")
            .replace(/dom/g, "DOM")
            .replace(/html/g, "HTML")
            .replace(/css/g, "CSS")
            .replace(/dns/g, "DNS")
            .replace(/etag/g, "ETag");
    },

    validate: function (res) {
        return typeof res === 'object' && typeof res.weights === 'object' && typeof res.tests === 'object' && typeof res.summary === 'object';
    },

    set: function (res) {
        if (!this.weightsComplete) {
            this.setWeights(res.weights);
        }

        if (!this.pageTestsCompleted) {
            this.setPageTestResults(res);
        }

        if (res.summary.completed) {
            this.setFinalResults(res);
        }

        this.completed = this.weightsCompleted && res.summary.completed;
        return this.completed;
    },

    setWeights: function (res) {
        var completed = true;
        for (var i = 0; i < TestDevices.length; i++) {
            if (res[i]) {
                if ($.inArray(i, results.weightsCompletedDevices) > -1) {
                    continue;
                }
                results.weightsCompletedDevices.push(i);

                var info = res[i],
                    weightSum = info.markup + info.css + info.image + info.script + info.other,
                    weightSumChart = weightSum / 100,
                    weightEncSum = info.markupenc + info.cssenc + info.imageenc + info.scriptenc + info.otherenc,
                    weightEncSumChart = weightEncSum / 100;

                $('#prism-bar-markup' + i).attr('aria-valuetransitiongoal', Math.round(info.markupenc / weightEncSumChart));
                $('#prism-bar-image' + i) .attr('aria-valuetransitiongoal', Math.round(info.imageenc / weightEncSumChart));
                $('#prism-bar-css' + i)   .attr('aria-valuetransitiongoal', Math.round(info.cssenc  / weightEncSumChart));
                $('#prism-bar-script' + i).attr('aria-valuetransitiongoal', Math.round(info.scriptenc / weightEncSumChart));
                $(".progress-bar").progressbar();

                weightEncSum = Math.round((weightEncSum / 1024) * 10) / 10;
                weightSum = Math.round((weightSum / 1024) * 10) / 10;

                $('#prism-requests' + i).html(info.requests);
                $('#prism-weight' + i).html(weightSum);
                $('#prism-weightenc' + i).html(weightEncSum);
                if (i == 1) {
                    results.highEndWeight = weightEncSum;
                }
                if (info.url && info.url.indexOf('chrome') !== 0) {
                    prism.loadFrame(info.url, i);
                }
            } else {
                completed = false;
            }
        }

        if (completed) {
            results.weightsCompleted = true;
            $('.weight').fadeIn('fast', function() { });
        }
    },

    setPageTestResults: function (res) {
        var summary = res.summary;
        var tests   = res.tests;

        if (tests.length > 0) {
            this.pageTestsCompleted = true;
        }

        var navbarTop = $('#navbar-top');
        if (typeof home !== 'undefined') {
            navbarTop.removeClass();
        }
        navbarTop.removeAttr("role");
        $('body').css('margin-top', '0');
        $('footer').addClass('initfooter');
        $('.testURL').html(summary.url);
        $('#testDate').html(summary.finished);
        $('#textOverallScore').html('calculating...');

        $('#breakdownFails, #breakdownWarns, #breakdownPasses').html('');

        results.failCount = 0;
        results.warnCount = 0;
        results.passedCount = 0;
        var comments = {pass: '', warn:'', fail:''},
            type;

        for (var i = 0; i < tests.length; i++) {
            if (tests[i].passed) {
                results.passedCount++;
                type = 'pass';
            } else {
                if (tests[i].warning) {
                    results.warnCount++;
                    type = 'warn';
                } else {
                    results.failCount++;
                    type = 'fail';
                }
            }

            tests[i].definition.name = results.getTestTitle(tests[i].definition.name);
            tests[i].type = type;
            comments[type] += '<li class="' + type + '"><a class="breakdownDetailsLink" type="' + type + '" tid="' + i + '">' + tests[i].definition.name + '</a></li>';
        }

        results.tests = tests;
        comments = comments['fail'] + comments['warn'] + comments['pass'];

        $('#breakdownList').append(comments);

        if (tests.length) {
            $('#failsKnob').val(results.failCount).knob();
            $('#warningsKnob').val(results.warnCount).knob();
            $('#passesKnob').val(results.passedCount).knob();
        }

        $('.breakdownDetailsLink').click(function(e) {
            e.preventDefault();
            mobiReady.showBreakdownDetails(e);
        });
        $('.breakdownDetailsLink:first').click();


        $('.tooltiplink').tooltip();
        $('.results').fadeIn();

        window.location.hash = '#' + mobiReady.sid;

        if (mobiReady.getParameterByName(window.location.href, 'pdf') == '1') {
            mobiReady.formatPdfReport();
        }

        if (typeof home !== 'undefined' && home.isLandingPageTest == 1) {
            home.isLandingPageTest = 2;
        }

        if (comments) {
            $('#techbreakdown').show();
        }
    },

    setFinalResults: function (res) {
        var textWriter = $('#test-results-waiter-text'),
            textWriterSpan = textWriter.find('span');

        $('#result-rank, #result-summary').show();

        textWriterSpan.fadeOut('fast', function () {
            textWriter
                .css('background-color', '#FFFFFF')
                .css('padding-left', '0')
                .css('padding-right', '0')
                .html("<a id='navbar-results-link' href=''>View Test Results</a>");

            $('#navbar-results-link').click(function (e) {
                e.preventDefault();
                mobiReady.navigateToElement('results');
            });

            textWriterSpan.fadeIn('slow');
            $('#test-results-waiter-spinner').fadeOut('slow');
        });

        var score = res.summary.score;
        var msg;
        if (score < 1.0) {
            msg = 'The tested page performed extremely poorly, scoring only ' + score + '. ';
        } else if (score < 2) {
            msg = 'The tested page performed very poorly in mobile readiness tests, scoring only ' + score + ' out of 5. ';
        } else if (score < 3) {
            msg = 'The tested page performed well scoring ' + score + ' out of 5 but there is considerable room for improvement. ';
        } else if (score < 4) {
            msg = 'The tested page scored very well at ' + score + ' out of 5 but could still be made more mobile mobiReady. ';
        } else if (score == 5.0) {
            msg = 'Top score, 5.0! This page is certainly mobiReady for the mobile era and should perform well across all device types. ';
        } else {
            msg = 'The tested page performed excellently with a score of ' + score + ' out of 5 but there is still room for improvement. ';
        }

        $('#textOverallScore').html(score);
        results.plotRanks(score, res.summary.url);

        $('#scoreKnob').val(Math.ceil(score)).knob();

        if (results.failCount == 0) {
            msg += 'No significant test failures were detected, this is excellent. ';
            $('#breakdownFails').replaceWith("<span><em>No failures!</em><span>");
        } else if (results.failCount > 5) {
            msg += 'Many significant test failures were detected, these are likely to directly impact the user. ';
        } else {
            msg += 'Some serious test failures were detected and should be addressed. ';
        }
        if (results.warnCount == 0) {
            $('#breakdownWarns').replaceWith('<span><em>No warnings!</em><span>');
        }
        if (msg.length == 0) {
            $('#breakdownPasses').replaceWith('<span><em>No passed tests.</em><span>');
        }

        msg += results.buildPageWeightBar();

        $('#execSummary').html(msg);
    },

    plotRanks: function (score) {
        var pointX = score.toFixed(1);
        var pointY = 0;

        $.ajax({
            url: mobiReady.API_BASE_URL + 'benchmark/get-chart-data',
            cache: false,
            type: 'get',
            success: function (res) {
                if (typeof res !== 'object') {
                    return;
                }

                var dataAll = [],
                    found = false;
                for (var i = 0, l = res.length; i < l; i++) {
                    dataAll.push([res[i].x, res[i].y]);
                    // find an appropriate y for the current score
                    if (!found && pointX <= res[i].x) {
                        found = true;
                        pointY = res[i].y;
                    }
                }

                var dataScore = [[pointX, pointY]],
                    plot = $.plot(
                        '#placeholder',
                        [
                            {
                                color: "#1db4ac",
                                data: dataAll,
                                lines: {show: true},
                                points: {show: false}
                            },
                            {
                                color: "#1db4ac",
                                data: dataScore,
                                lines: {show: true},
                                points: {show: true}
                            }
                        ],
                        {
                            xaxis: {
                                ticks: [0, 1, 2, 3, 4, 5],
                                tickDecimals: 0,
                                max: 5,
                                min: 0
                            },
                            yaxis: {
                                tickFormatter: function () {
                                    return '';
                                }
                            },
                            grid: {
                                backgroundColor: {colors: ["#fff", "#fff"]},
                                borderWidth: {
                                    top: 0,
                                    right: 0.2,
                                    bottom: 0.5,
                                    left: 0.5
                                },
                                markings: [
                                    {color: "#fdfdfc", yaxis: {from: 1}},
                                    {color: "#fdfdfc", yaxis: {from: 0, to: 0.25}},
                                    {color: "#fdfdfc", yaxis: {from: 0.50, to: 0.75}}
                                ]
                            },
                            points: {
                                symbol: function (ctx, x, y, radius, shadow) {
                                    ctx.arc(x, y, radius * 2, 0, shadow ? Math.PI : Math.PI * 2, false);
                                }
                            }
                        }
                    );

                var o = plot.pointOffset({x: pointX, y: pointY});
                // append it to the placeholder that Flot already uses for positioning
                $('#placeholder').append(
                    '<div id="popoverRefPoint" style="position:absolute;left:' + o.left + 'px;top:' + (o.top - 7) + 'px;color:#666;font-size:smaller">&nbsp;</div>'
                );
                var popOver = $('#popoverRefPoint');

                popOver.popover('destroy');
                popOver.popover({
                    content: '<p id="bellBalloonTxt">YOU<br/>SCORED<br/><br/></p><p id="bellBalloon">' + score + '</p>',
                    placement: 'top',
                    html: true
                });
                popOver.popover("show");
            }
        });
    },

    buildPageWeightBar: function () {
        var pageSize = Math.floor(results.highEndWeight),
            pageSizeDesc,
            msg;

        if (pageSize > 750) {
            pageSizeDesc = "Page weight is too large, <a id='balloon-results-details-link' href='https://mobiforge.com/research-analysis/understanding-web-page-weight'>and here's why</a>";
            msg = "The tested page weight, in terms of bytes downloaded, is very high and user experience will suffer as a result. ";
            $(".progress-bar-danger").html('');
        } else if (pageSize > 500) {
            pageSizeDesc = "Page weight is high.";
            msg = "The page weight, in terms of bytes downloaded, is higher than would be considered optimal. Any efforts to lower the page weight will improve user experience. "
        } else {
            pageSizeDesc = "Page weight is acceptably low.";
            msg = "The page download weight is acceptably low, this is likely to improve user experience. ";
        }

        $('#progress-bar-site').css({'background-color': 'transparent'});
        $('.pageSize').html(pageSize);

        var balloonPageSizeText = "<b>" + pageSize + "KB</b>";
        var balloonPositionLeft = ((pageSize * 10) / 75) - 3;
        var balloonArrowPositionLeft = "10%";

        var pbarOverFlow = $(".progress-bar-overflow"),
            maxPageSize = $('#maxPageSize');

        maxPageSize.html('<label style="color:red">750KB</label>');

        pbarOverFlow
            .css("z-index", "0")
            .css("background-color", "#fb2515")
            .css("width", "0%");

        $('#progressNeedleWeight1').css('display', '');

        if (pageSize > 600 && pageSize <= 750) {
            //fix balloon
            var diff = balloonPositionLeft - 77;
            balloonPositionLeft = 77;
            balloonArrowPositionLeft = ((diff * 4) + 13) + "%";
        } else if (pageSize > 750) {
            //bar 2
            balloonPositionLeft = 69;
            balloonArrowPositionLeft = "77%";
            balloonPageSizeText = '<label style="color:red">' + balloonPageSizeText + '</label>';

            maxPageSize.html(balloonPageSizeText);

            pbarOverFlow
                .css('width', '100%')
                .css('z-index', 2)
                .css('background-color', 'rgb(250, 29, 22, 0)')
                .css('background-image', '');
        }

        //page weight balloon
        $('#result-page-weight').show();

        var progressWeight = $('#progressWeight');
        progressWeight.popover("destroy");
        var popoverContainer = progressWeight.popover({
            content: balloonPageSizeText + ' - ' + pageSizeDesc,
            placement: 'top',
            html: true
        });

        progressWeight.popover('show');
        var pp = $('.arrow', $(popoverContainer).siblings()),
            ae = pp.parent(),
            minWidth;

        ae.css('left', balloonPositionLeft + '%');

        if (pageSize <= 100) {
            minWidth = 310;
        } else if (pageSize <= 500) {
            minWidth = 310;
        } else if (pageSize <= 750) {
            minWidth = 240;
        } else {
            minWidth = 390;
        }

        ae.css('min-width', minWidth + 'px');
        pp.css('left', balloonArrowPositionLeft);

        $('#balloon-results-details-link').click(function(e) {
            e.preventDefault();
            mobiReady.navigateToElement('techbreakdown');
        });

        return msg;
    }

};

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
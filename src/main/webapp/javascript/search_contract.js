/**
 * Created by berz on 07.04.14.
 */
var searchContract = (function (msg, pref) {

    return function () {
        var ajaxMessages = msg;
        var urlPrefix = pref;

        helper.urlPrefix = urlPrefix;


        $('a.keySort').click(function () {
            data = $(this).attr('href').split("|");
            $("form#searchCond input[name=orderColumn]").val(data[0]);
            $("form#searchCond input[name=orderType]").val(data[1]);

            $('tr#searchTr input[type=submit]').click();

            return false;
        });

        $('tr#searchTr input#reset').click(function () {
            $('tr#searchTr input[type=text],form#searchCond input[type=hidden], tr#searchTr select').each(function () {
                $(this).val('');
            });

            $('tr#searchTr input[type=submit]').click();

        });


        $('tr#searchTr input[type=submit]').click(function () {


            //alert(JSON.stringify(searchData));
            helper.loadPage();


            return false;
        });


        $('tr#searchTr input[name=excel_btn]').click(function () {

            var form = document.createElement("form");
            var inputFilter = document.createElement("input");
            var inputExcel = document.createElement("input");

            $(inputFilter).attr("type", "text");
            $(inputFilter).attr("name", "filter");
            $(inputFilter).attr("value", helper.getFilter());

            $(inputExcel).attr("type", "text");
            $(inputExcel).attr("name", "excel");
            $(inputExcel).val("true");

            $(form).append(inputFilter).append(inputExcel);
            $(form).attr("method", "post");
            $(form).attr("action", urlPrefix + "contracts?ajaxList");

            document.body.appendChild(form);
            form.submit();
            document.body.removeChild(form);

        });


    };
})(ajaxMessages, urlPrefix);

$(document).ready(searchContract);


var helper = {

    urlPrefix : '',

    currentState: {
        'filter': null,
        'page': null,
        'size': null
    },

    defaultState: {
        'page': 1,
        'size': 50
    },

    loadCatContractIDs: function(){
        var ccSel = $('select[name=catContract]');
        var res = {};

        if(ccSel.length > 0){
            $('option', ccSel).each(function(i,e){
                res[$(e).text()] = e.value;
            });

            return res;
        }

        return null;
    },

    loadProfileData: function(profile){
        var catContractIDs = this.loadCatContractIDs();

        if(catContractIDs == null) return {};

        if(profile != null){
            return {
                'catContract' : [catContractIDs[profile]]
            };
        }

        return {};
    },

    getFilter: function () {
        var formData = {};
        var profileData = {};

        if($('select[data-selector="excel_profile"]').val() != 'null'){
            profileData = this.loadProfileData($('select[data-selector="excel_profile"]').val());
        }

        $('tr#searchTr input[type=text], form#searchCond input[type=hidden], tr#searchTr select').each(function () {
            formData[this.name] = $(this).val();
        });


        var searchData = {
            'number': (formData.c_number != "") ? formData.c_number : null,
            //'catContract' : formData.catContract.join(','),
            'catContract': (typeof(profileData.catContract) != 'undefined' )? profileData.catContract : formData.catContract,
            'catContractStatus': formData.catContractStatus,
            'paymentWay': formData.paymentWay,
            'person': (formData.person != "") ? formData.person : null,
            'partner': (formData.partner != "") ? formData.partner : null,
            'creator': (formData.creator != "") ? formData.creator : null,
            'startDateFrom': formData.startDateFrom,
            'startDateTo': formData.startDateTo,
            'isApp': (formData.appDate == "yes") ? true : (formData.appDate == "no" ? false : null),
            'isPrinted': (formData.printDate == "yes") ? true : (formData.printDate == "no" ? false : null),
            'isPaid': (formData.payDate == "yes") ? true : (formData.payDate == "no" ? false : null),
            'orderColumn': formData.orderColumn,
            'orderType': formData.orderType
        };

        return JSON.stringify(searchData);
    },

    pageLineButtons: function () {
        $('div#searchResult tr.footer a').each(

            (function (t) {
                return function (i, e) {
                    pageData = t.getPageDataFromUrl($(e).attr('href'));
                    $(e).click(
                        (function (pd) {
                            return function () {
                                t.loadPage(pd);

                                return false;
                            }
                        })(pageData)
                    );

                }
            })(this)


        );

    },

    getPageDataFromUrl: function (url) {
        pd = {
            'page': null,
            'size': null
        };

        if (url.split('?').length > 1) {
            req = url.split('?')[1];

            vars = req.split('&');
            if (vars.length > 0)
                for (i in vars) {
                    if (vars[i].split('=').length > 1) {
                        if (vars[i].split('=')[0] == 'page') {
                            pd.page = vars[i].split('=')[1];
                        }
                        if (vars[i].split('=')[0] == 'size') {
                            pd.size = vars[i].split('=')[1];
                        }
                    }
                }
        }


        return pd;
    },

    loadPage: function (pageData) {
        var filter = this.getFilter();

        if (typeof(pageData) == 'undefined') {
            pageData = this.defaultState;
        }

        url = urlPrefix + "contracts?ajaxList";

        if (pageData.page != null)
            url += '&page=' + pageData.page;

        if (pageData.size != null)
            url += '&size=' + pageData.size;

        $('table tbody').html(
            '<tr><td><img src="' + this.urlPrefix + 'images/ajax_load.gif"></td></tr>'
        );

        $.post(
            url,
            {
                'filter': filter
            },
            (function (t) {
                return function (d) {
                    // var win = window.open();
                    // win.document.write(d);

                    var tb = $('table tbody', d).get(0);

                    var pagesLine = $('tr.footer', d);

                    if (tb != null) {
                        $('table tbody').html($(tb).html());
                        $(tb).append(pagesLine);
                    }
                    else {
                        $('table tbody').html('');
                    }

                    t.currentState.filter = filter;
                    t.currentState.page = 1;
                    t.pageLineButtons();

                    //initEvents();
                }
            })(this)

        );


    }
}
var domReadyPersons = (function (pref, msg, imgOnload) {
    return function () {
        var urlPrefix = pref;
        var ajaxMessages = msg;


        personWindow.init({ajaxMessages: msg, urlPrefix: pref, onLoadImg: imgOnload});

        $("#fill_person").click(
            function () {
                if(typeof($(this).attr('data-cat-contract')) != "undefined"){
                    personWindow.catContract = $(this).attr('data-cat-contract');
                }
                personWindow.openCleanFormWindow(function () {
                    personWindow.loadPersonForm(null)
                });
                return false;
            }
        );

        $("#personValue a").click(
            function () {
                if(typeof($(this).attr('data-cat-contract')) != "undefined"){
                    personWindow.catContract = $(this).attr('data-cat-contract');
                }
                personWindow.openCleanFormWindow(
                    (function (h) {
                        return function () {
                            personWindow.loadPersonForm(h)
                        }
                    })(this.href)
                );

                return false;
            }
        );
    }
})(urlPrefix, ajaxMessages, "images/ajax_load.gif");

$(document).ready(domReadyPersons);


var personWindow = {

    catContract: null,
    outerParams: null,
    buttons: {
        SEARCH: 0,
        SAVE: 1,
        CLEAR: 2
    },

    getButtons: function () {
        return this.buttons;
    },

    bindButtons: function () {
        $("form#controls input#check_btn").click(
            (function (t) {
                return function () {
                    t.search();
                }
            })(this)
        );
        $("form#controls input#save_choosen").click(
            (function (t) {
                return function () {
                    t.save();
                }
            })(this)
        );
        $("form#controls input#clear").click(
            (function (t) {
                return function () {
                    t.clear();
                }
            })(this)
        );


    },

    init: function (out) {
        outerParams = out;
    },

    configuration: function () {
        return outerParams;
    },

    renderError: function (error) {
        if (error != null) {
            var p = document.createElement("p");
            p.innerHTML = error;
            $("div#errors_bar").empty().append(p);
        }
    },

    fillDataInForm: function (data) {
        for (fieldName in data) {
            if ($("form#person input[name=" + fieldName + "]").length > 0) {
                if (fieldName == "sex") {
                    if (data[fieldName] == "FEMALE") $("form#person input[name=sex][value=FEMALE]").prop("checked", true);
                }
                else {
                    $("form#person input[name=" + fieldName + "]").val(data[fieldName]);
                }
            }
        }
    },

    cleanWindowData: null,

    newPersonFormData: null,

    openCleanFormWindow: function (callback) {
        var cb = (function (t) {
            return function () {
                callback();
                t.bindButtons();
            }
        })(this);
        // body
        if (this.cleanWindowData == null) {
            $.ajax({
                'url': outerParams.urlPrefix + "persons?clean",
                'success': (function (t, c) {
                    return function (d) {
                        t.cleanWindowData = d;
                        $.fancybox({
                            'content': d,
                            'afterShow': typeof(c) == "function" ? c : null
                        });
                    }
                })(this, cb)
            });
        }
        else {
            $.fancybox({
                'content': this.cleanWindowData,
                'afterShow': typeof(cb) == "function" ? cb : null
            });
        }
    },

    getFormWindowContent: function () {
        return $("div#personAjaxForm").html();
    },

    loadPersonForm: function (personHref) {
        this.setFormIsLoading();
        var data = {};
        if(this.catContract != null){
            data.cc_id = this.catContract;
        }
        if (personHref != null) {
            // форма с данными страхователя
            $.ajax({
                'url': personHref,
                'data' : data,
                'success': (function (t) {
                    return function (d) {
                        $("div#mainPersonDataForm").html(d);
                        $.fancybox.update();

                        t.disableButton(t.buttons.SEARCH);
                        t.enableButton(t.buttons.SAVE);
                        t.enableButton(t.buttons.CLEAR);

                        t.setDatePickersAndFilters();
                    }
                })(this)
            });
        }
        else {
            // форма создания нового страхователя
            if (this.newPersonFormData == null) {
                $.ajax({
                    'url': outerParams.urlPrefix + "persons?form",
                    'data' : data,
                    'success': (function (t) {
                        return function (d) {
                            t.newPersonFormData = d;
                            $("div#mainPersonDataForm").html(d);
                            $.fancybox.update();

                            t.enableButton(t.buttons.SEARCH);
                            t.disableButton(t.buttons.SAVE);
                            t.disableButton(t.buttons.CLEAR);

                            t.setDatePickersAndFilters();
                        }
                    })(this)
                });
            }
            else {
                $("div#mainPersonDataForm").html(this.newPersonFormData);

                this.enableButton(this.buttons.SEARCH);
                this.disableButton(this.buttons.SAVE);
                this.disableButton(this.buttons.CLEAR);

                $.fancybox.update();

                this.setDatePickersAndFilters();

            }
        }
    },

    setFormIsLoading: function () {

        if (outerParams.onLoadImg != null) {
            var img = document.createElement('img');
            img.src = outerParams.urlPrefix + outerParams.onLoadImg;
            $("div#mainPersonDataForm").empty().append(img);
        }
    },

    disableButton: function (b) {

        if (b == this.buttons.SEARCH) {
            $("form#controls input#check_btn").attr('disabled', true);
        }

        if (b == this.buttons.CLEAR) {
            $("form#controls input#clear").attr('disabled', true);
        }

        if (b == this.buttons.SAVE) {
            $("form#controls input#save_choosen").attr('disabled', true);
        }
    },

    enableButton: function (b) {

        if (b == this.buttons.SEARCH) {
            $("form#controls input#check_btn").attr('disabled', false);
        }

        if (b == this.buttons.CLEAR) {
            $("form#controls input#clear").attr('disabled', false);
        }

        if (b == this.buttons.SAVE) {
            $("form#controls input#save_choosen").attr('disabled', false);
        }
    },

    searchInProgress: function (turnoff) {
        if (turnoff != null && turnoff) {
            $("div#search_progress").empty();
            return;
        }

        if (outerParams.onLoadImg != null) {
            var img = document.createElement("img");
            img.src = outerParams.urlPrefix + outerParams.onLoadImg;

            $("div#search_progress").empty().append(img);
        }
    },

    updatePersonLink: function (person) {
        if (person == null) {
            // Ссылка на создание страхователя
            $('a#fill_person').css('display', 'block');
            $('p#personValue').css('display', 'none');
            $('input[name=person]').val(0);
        }
        else {
            // Ссылка на редактирование страхователя
            $('a#fill_person').css('display', 'none');
            $('p#personValue').css('display', 'block');

            $("p#personValue").html("<a href='" + this.configuration().urlPrefix + "persons/edit?id=" + person.id + "&ajax=1' >" +
                person.surname + ' ' +
                person.name + ' ' +
                person.middle + ' ' +
                person.born +
                "</a>");

            $('input[name=person]').val(person.id);

            $("p#personValue a").click(
                (function (t) {
                    return function () {
                        t.openCleanFormWindow(
                            (function (tt, link) {
                                return function () {
                                    tt.loadPersonForm(link.href);
                                }
                            })(t, this)
                        );

                        return false;
                    }
                })(this)
            );
        }
    },

    save: function () {
        var data = this.getData();

        var action = $("form#person").attr('action');

        this.setFormIsLoading();

        $.ajax({
            "type": "POST",
            "url": action,
            "data": data,
            "success": (function (t) {
                return function (d) {
                    var res = JSON.parse(d);

                    if (res.success) {
                        t.updatePersonLink(res.person);
                        $.fancybox.close();
                    }
                    else {
                        if (res.errorCode != null) {
                            t.renderError(t.configuration().ajaxMessages[res.errorCode]);
                        }
                        else {
                            t.renderError(res.message);
                        }

                        t.loadPersonForm();

                        t.fillDataInForm(data);
                    }
                }
            })(this)
        });

    },
    search: function () {
        var data = this.getData();

        this.emptyFoundPeoplesList();
        this.searchInProgress();

        $.ajax({
            "type": "POST",
            "url": outerParams.urlPrefix + "persons?search",
            "data": data,
            "success": (function (t) {
                return function (d) {
                    t.searchInProgress(true);

                    var res = JSON.parse(d);
                    if (res != null) {
                        t.enableButton(t.buttons.SAVE);
                        //t.loadPersonForm();
                        if (res.length > 0) {
                            t.showFoundPeopleList();
                            for (i in res) {
                                var obj = res[i];
                                $("form#foundPeopleForm table").append("<tr><td><a href='' id='person" + obj.id + "'>" + obj.info + "</a></td></tr>");

                                $.fancybox.update();


                                $("#person" + obj.id).click(
                                    (function (o, t) {
                                        return function () {
                                            link = t.configuration().urlPrefix + "persons/edit?id=" + o.id + "&ajax=1";
                                            t.loadPersonForm(link);
                                            return false;
                                        }
                                    })(obj, t)
                                );
                            }
                        }
                        else t.hideFoundPeopleList();
                    }
                }
            })(this)
        });
    },
    clear: function () {
        this.loadPersonForm();
        this.emptyFoundPeoplesList();
        this.hideFoundPeopleList();

        this.updatePersonLink();

        this.enableButton(this.buttons.SEARCH);
        this.enableButton(this.buttons.SAVE);
        this.disableButton(this.buttons.CLEAR);
    },

    getData: function () {
        var formData = {};
        $("form#person input[type=text]").each(function () {
            if (this.name) formData[this.name] = this.value;
        });

        $("form#person input[type=radio]:checked").each(function () {
            if (this.name) formData[this.name] = this.value;
        });

        if ($("form#person input[name=_method]").length > 0) formData['_method'] = $("form#person input[name=_method]").val();

        if ($("form#person input[name=id]").length > 0) formData['id'] = $("form#person input[name=id]").val();

        return formData;
    },

    emptyFoundPeoplesList: function () {
        $("div#foundPeople table").empty();
    },

    showFoundPeopleList: function () {
        $("div#foundPeople").css('display', 'block');
    },

    hideFoundPeopleList: function () {
        $("div#foundPeople").css('display', 'none');
    },

    setDatePickersAndFilters: function () {
        $('form#person div.date input[type=text]').datepicker({
            dateFormat: 'dd.mm.yy'
        }).on('change', function (e) {
                if (!/[0-9]{2}\.[0-9]{2}\.[0-9]{4}/.test($(this).val()) && $(this).val() != '') {
                    $(this).val('');
                    alert('Неправильный формат даты!');
                    return false;
                }
            });

        filters();

        $('#addr_equals').click(function () {
            $("form#person input[name=indexResident]").val($("form#person input[name=indexRegistr]").val());
            $("form#person input[name=addrResident]").val($("form#person input[name=addrRegistr]").val());
            $("form#person input[name=cityResident]").val($("form#person input[name=city]").val());

            return false;
        });

        /*var projects = [{
            value: "115563",
            label: "Москва"
        }, {
            value: "22222",
            label: "Анапа"
        }, {
            value: "33333",
            label: "Волгоград"
        }];*/


        $("form#person input[name=cityResident]").autocomplete({
            source: (function(t){
                return function(request, response){
                    $.ajax({
                        url : t.configuration().urlPrefix + "persons?getCitiesAndIndexes",
                        method : "POST",
                        dataType: "json",
                        data: request,
                        success : function(d){
                            response(d);
                        }
                    })
                }
            })(this),
            select: function(event,ui){

                $("form#person input[name=cityResident]").val(ui.item.label);
                $("form#person input[name=indexResident]").val(ui.item.value);

                return false;
            },
            create: function () {
                $(this).data('ui-autocomplete')._renderItem = function (ul, item) {
                    return $('<li>')
                        .append('<a>' + item.label + '<br>' + item.value + '</a>')
                        .appendTo(ul);
                };
            }
        });

        $("form#person input[name=city]").autocomplete({
            source: (function(t){
                return function(request, response){
                    $.ajax({
                        url : t.configuration().urlPrefix + "persons?getCitiesAndIndexes",
                        method : "POST",
                        dataType: "json",
                        data: request,
                        success : function(d){
                            response(d);
                        }
                    })
                }
            })(this),
            select: function(event,ui){

                $("form#person input[name=city]").val(ui.item.label);
                $("form#person input[name=indexRegistr]").val(ui.item.value);

                return false;
            },
            create: function () {
                $(this).data('ui-autocomplete')._renderItem = function (ul, item) {
                    return $('<li>')
                        .append('<a>' + item.label + '<br>' + item.value + '</a>')
                        .appendTo(ul);
                };
            }
        });
    }
};
var domReadyInsuranceAreas = (function (pref, msg, imgOnload) {
    return function () {
        var urlPrefix = pref;
        var ajaxMessages = msg;

        $('div#ins_areas a').click(
            function () {

                insuranceAreasWindow.setForcedReloading(true);
                insuranceAreasWindow.load(this.href, "insuranceObjects", "updateInsObj");

                return false;
            }
        );

        $('div#premiumsLink a').click(
            function(){
                insuranceAreasWindow.setForcedReloading(false);
                insuranceAreasWindow.load(this.href, "premiums", "updPremiumsWindow");

                return false;
            }
        );



    }
})(urlPrefix, ajaxMessages, "images/ajax_load.gif");

$(document).ready(domReadyInsuranceAreas);

var insuranceAreasWindow = {

    url: null,
    divID: null,
    updateLinkID: null,
    forcedReloading: true,

    setForcedReloading: function(fr){
        this.forcedReloading = fr;
    },

    load: function (url, divID, updateLinkID) {

        if (typeof(divID) !== 'undefined')
            this.divID = divID;
        if (typeof(updateLinkID) !== 'undefined')
            this.updateLinkID = updateLinkID;

        $.ajax({
            'url': url,
            'success': (function (t) {
                return function (d) {
                    t.render(d);
                    t.url = url;
                    t.bindActions(url);
                };
            })(this)

        });
    },

    render: function(content){
        $.fancybox({
            'content': content
        });
    },

    bindActions: function (url) {
        $('div#' + this.divID + ' form').each(
            (function (t, u) {
                return function () {
                    // Перехватываем отправку формы
                    $(this).submit(function () {

                        // Отправляем запрос и перезагружаем окно
                        $.ajax({
                            'url': this.action,
                            'type': this.method,
                            'data': (function (t) {
                                var data = {};
                                $('input[type=text], input[type=hidden], select', t).each(function () {
                                    data[this.name] = this.value;
                                });

                                //alert('input[type=text,form=' + $(t).attr('id') + ']');

                                $('input[type=text][form=' + $(t).attr('id') + ']').each(function () {
                                    data[this.name] = this.value;
                                });

                                return data;
                            })(this),
                            'success': function (d) {
                                t.reload(url, d);
                            }
                        });

                        setTimeout(function () {
                            //t.reload(url);
                        }, 100);

                        return false;
                    });
                }
            })(this, url)
        );

        if (this.divID != null) {
            $('a#updateInsObj').click(
                (function (t) {
                    return function () {
                        t.reload(t.url);
                        return false;
                    };
                })(this)
            );
        }

        filters();
    },

    close: function () {
        $.fancybox.close();
    },

    reload: function (url, content) {
        this.close;
        if(this.forcedReloading){
            this.load(url);
        }
        else{
            this.render(content);
            this.bindActions();
        }
    }

}
var domReadyImages = (function (pref, msg, imgOnload) {
    return function () {
        var urlPrefix = pref;
        var ajaxMessages = msg;

        $('ul#images a.imgedit').click(
            function(){
                id = $(this).parents("li").eq(0).attr('id');

                imagesWindow.load(this.href, id);

                return false;
            }
        );



    }
})(urlPrefix, ajaxMessages, "images/ajax_load.gif");

$(document).ready(domReadyImages);

var imagesWindow = {

    url : null,
    id: null,


    load : function(url, id){

        $.ajax({
            'url' : url,
            'success' : (function(t){
                return function(d){
                    $.fancybox({
                        'content' : d
                    });

                    t.url = url;
                    t.id = id;
                    t.bindActions(url);
                };
            })(this)

        });
    },

    bindActions : function(url){
        $('div#imagesEdit form').each(
            (function(t,u){
                return function(){
                    // Перехватываем отправку формы
                    $(this).submit(function(){

                        // Отправляем запрос и перезагружаем окно
                        $.ajax({
                            'url' : this.action,
                            'type' : this.method,
                            'data' : (function(t){
                                var data = {};
                                $('input[type=text], input[type=hidden], select', t).each(function(){
                                    data[this.name] = this.value;
                                });

                                if(typeof data['description'] !== 'undefined'){
                                    $('li#' + id + ' img').attr('title', data['description']);
                                    $('li#' + id + ' a').attr('title', data['description']);
                                    $('li#' + id + ' div.description p').html(data['description'].substr(0,16));
                                }

                                return data;
                            })(this)
                        });

                        setTimeout(function(){
                            t.close();
                        }, 100);

                        return false;
                    });
                }
            })(this, url)
        );


    },

    close : function(){
        $.fancybox.close();
    },

    reload : function(url){
        this.close;
        this.load(url);
    }

}
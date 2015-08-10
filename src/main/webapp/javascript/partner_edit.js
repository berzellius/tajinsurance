/**
 * Created by berz on 09.10.14.
 */

var domReadyPartnerEdit = (function (pref, msg, imgOnload) {
    return function () {
        var urlPrefix = pref;
        var ajaxMessages = msg;

        partnerEdit.loadingImgSrc = urlPrefix + imgOnload;

        $('.cc_block_container').each(function(){

            partnerEdit.loadCatContractBlock(


                $('form input[name=cc_id]', this).val(),
                '?params_cat_contract'
            );

        });




    }
})(urlPrefix, ajaxMessages, "images/ajax_load.gif");

$(document).ready(domReadyPartnerEdit);

var partnerEdit = {

    loadingImgSrc : null,

    loadCatContractBlock : function(catContractId, url){
        $.ajax({
            'url' : '?params_cat_contract',
            'data' : {
                'cc_id' : catContractId
            },
            'success' : (function(t){
                return function(d){
                    t.fillCatContractBlock(catContractId, d);
                }
            })(this)
        });
    },

    loadingStatusForBlock : function(catContractId){
        $('div#cc_block_'+catContractId).html(
            "<img src='" + this.loadingImgSrc + "' />"
        );
    },

    fillCatContractBlock : function(catContractId, data){
        $('div#cc_block_'+catContractId).html(data);
        this.bindBlockActions(catContractId);
    },

    bindBlockActions : function(catContractId){
        $('div#cc_block_'+catContractId+' form').submit(
            (function(t){
                return function(){

                    $.ajax({
                        url : $(this).attr('action'),
                        type : $(this).attr('method'),
                        data : (function(f){
                            var data = {};
                            $('input[type=text], input[type=hidden], select, textarea', f).each(function(){
                                data[this.name] = $(this).val();
                            });
                            $('input[type=checkbox]',f).each(function(){
                                data[this.name] = $(this).is(":checked");
                            })
                            return data;
                        })(this),
                        success : (function(f){
                            return function(d){

                                t.loadingStatusForBlock(catContractId);
                                t.fillCatContractBlock(catContractId, d);
                            }
                        })(this)
                    });

                    return false;
                }
            })(this)
        );


        $('a.franchises_link').click(
            (function(t){
                return function(){
                    t.openFranchiseDialog(this.href);

                    return false;
                }
            })(this)
        );

        filters();
    },

    openFranchiseDialog : function(url){
        $.ajax({
            'url' : url,
            'success' : (function(t){
                return function(d){
                    $.fancybox({
                        'content' : d
                    });

                    t.bindDialogActions();
                };
            })(this)

        });
    },

    bindDialogActions : function(){
        $("div#franchises form").each(
            (function(t){
                return function(){
                    // Перехватываем отправку формы
                    $(this).submit(function(){

                        // Отправляем запрос и перезагружаем окно
                        $.ajax({
                            'url' : this.action,
                            'type' : this.method,
                            'data' : (function(tf){
                                var data = {};
                                $('input[type=text], input[type=hidden], select', tf).each(function(){
                                    data[this.name] = this.value;
                                });

                                $('input, select',tf).attr('disabled', true);

                                return data;
                            })(this),
                            'success' : (function(tf){
                                return function(d){
                                    $.fancybox.close();

                                    $.fancybox({
                                        'content' : d
                                    });


                                    t.bindDialogActions();
                                }
                            })(this)
                        });



                        return false;
                    });
                }
            })(this)
        );

        filters();
    }

};
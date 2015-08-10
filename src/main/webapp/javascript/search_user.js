/**
 * Created by berz on 07.04.14.
 */
var searchContract = (function(msg,pref){

    return function(){
        var ajaxMessages = msg;
        var urlPrefix = pref;

        $('a.keySort').click(function(){
            data = $(this).attr('href').split("|");
            $("form#searchCond input[name=orderColumn]").val(data[0]);
            $("form#searchCond input[name=orderType]").val(data[1]);

            $('tr#searchTr input[type=submit]').click();

            return false;
        });

        $('tr#searchTr input#reset').click(function(){
            $('tr#searchTr input[type=text],form#searchCond input[type=hidden], tr#searchTr select').each(function(){
                $(this).val('');
            });

            $('tr#searchTr input[type=submit]').click();

        });

        $('tr#searchTr input[type=submit]').click(function(){



            var formData = {};

            $('tr#searchTr input[type=text], form#searchCond input[type=hidden], tr#searchTr select').each(function(){
                formData[this.name] = $(this).val();
            });

            var searchData = {
                'fio' : formData.fio,
                'username' : formData.username,
                'partner' : formData.partner,
                'authorities' : formData.authorities,
                'email' : formData.email,
                'enabled' : formData.enabled,
                'orderColumn' : formData.orderColumn,
                'orderType' : formData.orderType
            };

            //alert(JSON.stringify(searchData));

            $.post(
                urlPrefix + "users?list",
                {
                    'filter' : JSON.stringify(searchData)
                },
                function(d){
                   // var win = window.open();
                   // win.document.write(d);

                    var tb = $('table tbody',d).get(0);

                    if(tb != null){
                        $('table tbody').html($(tb).html());
                    }
                    else{
                        $('table tbody').html('');
                    }
                }
            );

            return false;
        });
    };
})(ajaxMessages, urlPrefix);

$(document).ready(searchContract);
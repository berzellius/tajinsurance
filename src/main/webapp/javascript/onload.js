/**
 * Created by berz on 30.03.14.
 */
var filters = function(){
    $('input.diglettersall').filter_input({regex:'[А-Яа-яЁёA-Za-z0-9]'});
    $('input.digits').filter_input({regex:'[0-9]'});
    $('input.email').filter_input({regex:'[A-Za-z0-9_\.\\-\\+\\?=#@]'});
    $('input.floatdigits').filter_input({regex:'[0-9\.]'});
    $('input.credit').filter_input({regex:'[А-Яа-яЁёA-Za-z0-9\.\+\-\/\\\\\*=\$\#\@\^\%\(\)\?\<\>\:\;]'});
}
$(document).ready(function(){
    filters();

    $("a.popup").click(function(){
        $.get(
            this.href,
            function(d){
                $.fancybox(d);
            }
        );

        return false;
    });
});

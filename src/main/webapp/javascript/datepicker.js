/**
 * Created by berz on 30.03.14.
 */
setTimeout(
    function () {
        $('div.date input[type=text], input.datepick').datepicker({
            dateFormat: 'dd.mm.yy'
        }).on('change',function(e){



                if (!/[0-9]{2}\.[0-9]{2}\.[0-9]{4}/.test($(this).val()) && $(this).val() != '') {
                    $(this).val('');
                    alert('Неправильный формат даты!');
                }
        });

    }, 1000
);
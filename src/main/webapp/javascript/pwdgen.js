/**
 * Created by berz on 10.04.14.
 */
var pwdGen = function() {
    var length = 12,
        charset = "abcdefghijklnopqrstuvwxyz0123456789",
        retVal = "";
    for (var i = 0, n = charset.length; i < length; ++i) {
        retVal += charset.charAt(Math.floor(Math.random() * n));
    }
    return retVal;
}

$(document).ready(function(){
    $('input#pwdGen').click(function(){
        $('input[name=password]').val(pwdGen());
    });
});

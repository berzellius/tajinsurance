/**
 * Created by berz on 29.03.14.
 */

var premiumCalculate = (function (pref) {
    return function () {
        var urlPrefix = pref;


        $('#buttonCalcRisk').click(function (e) {
            var request = {};
            e.stopPropagation();
            request.risk = $('#risk_allowed').val();
            request.sum = $('#risk_sum').val();
            request.contract = $('#contractIdField').val();
            request.length = $('#_length_id').val();

            if(request.length == 0){
                $('#_length_id').focus();return false;
            }

            $("#buttonCalcRisk").attr("disabled", true);
            $.post(
                urlPrefix + "risk?calc",
                request,
                function (d) {
                    var res = JSON.parse(d);
                    if (res.success) {
                        var obj = res.contractPremiumAjax;
                        $("#calcRisksTr").after("<tr id='pr" + obj.premiumId + "'><td>" + obj.risk + "</td><td>" +
                            obj.insuredSum + "</td><td>" + obj.premium + "</td><td><a href='" + urlPrefix + "risk?del&id="
                            + obj.premiumId + "' class='deletePremium'>Удалить</a></td></tr>");
                        $("#buttonCalcRisk").attr("disabled", false);
                        bindRiskActions();
                    }
                }
            );

            return false;
        });

        var bindRiskActions = function(){
            $('a.deletePremium').click(function () {
                $.get(this.href, function (d) {
                    var res = JSON.parse(d);
                    if (res.success) {
                        $('tr#pr' + res.id).remove();
                    }
                });

                return false;
            });
        };

        bindRiskActions();

    };
})(urlPrefix);

$(document).ready(premiumCalculate);

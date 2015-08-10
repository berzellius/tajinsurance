/**
 * Created by berz on 03.04.14.
 */
var contractLifeCycle = (function (msg, pref, sd, onlimg) {
    return function () {
        var ajaxMessages = msg;
        var urlPrefix = pref;
        var sumDescriptions = sd;
        var onLoadImg = onlimg;

        var checkIfWeCanPrintClaim = function () {
            if (
                    $('form#contract input[name=sum]').val() != "" &&
                    $('form#contract input[name=relatedContractNumber]').val() != "" &&
                        (
                            (!$('form#contract input[name=length]').attr('disabled'))?
                                $('form#contract input[name=length]').val() != "" : true
                            )       &&
                    $('form#contract input[name=person]').val() != "" &&
                    $('form#contract input[name=person]').val() != "0"

                ) {

                $("input#contractSave").attr('disabled',false);

                clearInterval(checkingIfWeCanPrintClaim);

            }
        }

        if($('input#claim_needed').length){
            var checkingIfWeCanPrintClaim = setInterval(function () {
                checkIfWeCanPrintClaim()
            }, 1000)
        }

        $('a#calcPremiumsWOSaving').click(function () {


            if ($('form#contract input[name=length]').val() == "") {
                $('form#contract input[name=length]').focus();
                return false;
            }

            /*if ($('form#contract input[name=sum]').val() == "") {
                $('form#contract input[name=sum]').focus();
                return false;
            }*/

            $.get(
                this.href,
                (function(t){
                    var data = {};
                    var fields = ["length", "sum", "franchise", "paymentType", "productRiskSet"];
                    var form = $('form#contract').get(0);

                    for(i in fields){
                        var f = fields[i];
                        if($('input[name='+f+'], select[name='+f+']',form).length > 0)
                            data[f] = $('input[name='+f+'], select[name='+f+']', form).val();

                    }
                    return data;
                })(this),
                function (d) {


                    var res = JSON.parse(d);
                    if (res.success) {

                        $('div#calcPremiums table td.result').html(res.sum);
                        $('div#calcPremiums').css('display', 'block');
                        $('div#errorBox').html("");

                        $('p#no_premium').html("");
                    }
                    else{
                        if(res.message == "maximum_sum_failed"){
                            $('div#errorBox').html("<p style='color: red'>"+ajaxMessages.SUM_TOO_BIG+"</p><p style='color: red'>" + ajaxMessages.MAX_SUM_IS + " " + res.maxSum + " TJS </p>");
                            //$('div#calcPremiums').css('display', 'block');
                        }
                        if(res.message == "minimum_sum_failed"){
                            $('div#errorBox').html("<p style='color: red'>"+ajaxMessages.SUM_TOO_SMALL+"</p><p style='color: red'>" + ajaxMessages.MIN_SUM_IS + " " + res.minSum + " TJS </p>");
                            //$('div#calcPremiums').css('display', 'block');
                        }
                        if(res.message == "minimum_term_failed"){
                            $('div#errorBox').html("<p style='color: red'>"+ajaxMessages.TERM_SMALL+"</p><p style='color: red'>" + ajaxMessages.MIN_TERM_IS + " " + res.minTerm + " " + ajaxMessages.MONTH + "</p>");
                            //$('div#calcPremiums').css('display', 'block');
                        }
                    }
                }
            );

            return false;
        });

        $('form#calcContractRisks').submit(function(){
            if(
                $('form#calcContractRisks input[name=length]').val() != "" &&
                $('form#calcContractRisks input[name=sum]').val() != ""
              )
            {
                $.post(
                    this.action,
                        /*"sum": $('form#calcContractRisks input[name=sum]').val(),
                        "length": $('form#calcContractRisks input[name=length]').val(),
                        "risk": $('form#calcContractRisks select[name=risk]').val()*/
                    (function(t){
                        var data = {};
                        $('input[type=text], input[type=hidden], select', t).each(function(){
                            v = $(this).val();

                            //alert(this.name + ": " + JSON.stringify(v) + " / " + data[this.name]);

                            if(typeof data[this.name] === 'undefined'){
                                data[this.name] = v;
                            }
                            else{
                                if($.isArray(data[this.name])){
                                    data[this.name].push(v);
                                }
                                if(typeof data[this.name] === 'string' ){
                                    data[this.name] = [data[this.name], v]
                                }
                            }


                        });

                        return data;
                    })(this),
                    function (d) {

                        var res = JSON.parse(d);
                        if (res.success) {
                            $('div#errors p').html('');

                            sum = res.sum;
                            sum_month = sum / $('form#calcContractRisks [name=length]').val();
                            sum_day = sum_month / 30;

                            insured_sum = res.insuredSum;

                            $('td',$('div#result table tr').eq(1)).eq(0).html(insured_sum);
                            $('td',$('div#result table tr').eq(1)).eq(1).html(sum);
                            $('td',$('div#result table tr').eq(1)).eq(2).html(sum_month);
                            $('td',$('div#result table tr').eq(1)).eq(3).html(sum_day);
                        }
                        else{
                            if(res.message == "maximum_sum_failed"){
                                $('div#errors p').html(ajaxMessages.SUM_TOO_BIG + ", " + ajaxMessages.MAX_SUM_IS + " " + res.maxSum + " TJS");
                            }
                            if(res.message == "minimum_sum_failed"){
                                $('div#errors p').html(ajaxMessages.SUM_TOO_SMALL + ", " + ajaxMessages.MIN_SUM_IS + " " + res.minSum + " TJS");
                            }
                            if(res.message == "minimum_term_failed"){
                                $('div#errors p').html(ajaxMessages.TERM_SMALL + ", " + ajaxMessages.MIN_TERM_IS + " " + res.minTerm + " " + ajaxMessages.MONTH);
                            }
                            if(res.message == "no_parametres_for_risk"){
                                $('div#errors p').html(ajaxMessages.NO_PARAM_TO_RISK);
                            }
                            $('td',$('div#result table tr').eq(1)).html('');
                        }
                    }
                );
            }

            return false;

        });

        $('input[type=button]#contractSave').click(function () {
            $('form#contract').submit();
        });

        $('input[type=button]#contractPrint').click(function () {
            var id = $('form#contract input[name=id]').val();

            $.get(
                urlPrefix + "contracts?print",
                {'id': id},
                function (d) {
                    var res = JSON.parse(d);
                    if (res.success) {
                        var win = window.open(urlPrefix + "contracts/" + id + "?print=yes");
                        window.location.href = urlPrefix + "contracts/" + id + "?form";
                    }
                    else alert(res.message);
                }
            );
        });

        $('input[type=button]#contractAccept').click(function(){
            var id = $('form#contract input[name=id]').val();

            window.location.href = urlPrefix + "contracts/" + id + "?accept";
        });

        $('input[type=button]#contractCancel').click(function () {
            var id = $('form#contract input[name=id]').val();
            $.get(
                urlPrefix + "contracts?cancel&id=" + id,
                function (d) {
                    var res = JSON.parse(d);
                    if (res.success) {
                        window.location.href = urlPrefix + "contracts/" + id + "/?form";
                    }
                }
            );
        });

        $('input[type=button]#contractClose').click(function () {
            window.location.href = urlPrefix + "contracts?page=1";
        });

        $('input[type=button]#printReceipt').click(function(){
            var id = $('form#contract input[name=id]').val();


            $.get(
                urlPrefix + "contracts?print_receipt",
                {'id': id},
                function (d) {
                    var res = JSON.parse(d);
                    if (res.success) {
                        var win = window.open(urlPrefix + "contracts/" + id + "?print_receipt");
                        window.location.href = urlPrefix + "contracts/" + id + "?form";
                    }
                    else alert(res.message);
                }
            );

        });


        var changeSumDescrition = function(){
            var cc_id = $("select#catContractSelector").val();
            if(cc_id == null) return;

            var img = document.createElement('img');
            img.src = urlPrefix + onLoadImg;
            $("div#calcPremiumForm").empty().append(img);

            $.get(
                urlPrefix + "contracts?get_calc_form",
                {
                    'cc_id' : cc_id
                },
                function(d){
                    $("div#calcPremiumForm").empty().html(d);
                    filters();
                }
            );
        };

        changeSumDescrition();

        $("select#catContractSelector").change(changeSumDescrition);

        $("#contractClaim").click(function(){


            window.open(urlPrefix + "persons?print_claim&id="
                +   $('form#contract input[name=person]').val()+"&cid=" +
                $('form#contract input[name=id]').val());
        });

        $("a.contract_img").fancybox();


    }
})(ajaxMessages, urlPrefix, sumDescriptions, "images/ajax_load.gif");

$(document).ready(contractLifeCycle);

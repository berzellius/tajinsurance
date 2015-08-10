package com.tajinsurance.web;

import com.tajinsurance.domain.ForbiddenReceiptNumber;
import com.tajinsurance.exceptions.AddForbiddenReceiptNumberException;
import com.tajinsurance.service.ForbiddenReceiptNumberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Created by berz on 07.07.14.
 */
@RequestMapping("/recnum")
@Controller
@RooWebScaffold(path = "recnum", formBackingObject = ForbiddenReceiptNumber.class)
public class ForbiddenReceiptNumberController {

    @Autowired
    ForbiddenReceiptNumberService forbiddenReceiptNumberService;

    @RequestMapping(method = RequestMethod.GET)
    public String getCurrentMonthNumbersPage(Model uiModel){

        List<ForbiddenReceiptNumber> forbiddenReceiptNumbers = forbiddenReceiptNumberService.getNumbersForCurrentMonth();

        uiModel.addAttribute("numbers", forbiddenReceiptNumbers);

        return "recnum/list";
    }

    @RequestMapping(method = RequestMethod.GET, params = "new_generation")
    public String addNewGeneration(Model uiModel){

        forbiddenReceiptNumberService.addNewGeneration();

        return "recnum/list";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String addNumberToCurrentNum(
            Model uiModel,
            @RequestParam(value = "number", required = true)
            Integer number
    ){

        try {
            forbiddenReceiptNumberService.addNumberForCurrentMonth(number);
        } catch (AddForbiddenReceiptNumberException e) {
            uiModel.addAttribute("reason", e.reason.toString());
            uiModel.addAttribute("forNumber", number);

            List<ForbiddenReceiptNumber> forbiddenReceiptNumbers = forbiddenReceiptNumberService.getNumbersForCurrentMonth();

            uiModel.addAttribute("numbers", forbiddenReceiptNumbers);

            return "recnum/list";
        }

        return "redirect:/recnum";

    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    public String deleteNumById(
            @PathVariable(value = "id") Long id,
            Model uiModel
    ){

        forbiddenReceiptNumberService.deleteNumberById(id);

        return "redirect:/recnum";

    }


}

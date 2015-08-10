package com.tajinsurance.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
/**
 * Created by berz on 24.03.14.
 */
@Controller
@RequestMapping("/ajax")
public class AjaxController {
    @RequestMapping(params = "test",method = RequestMethod.GET)
    public @ResponseBody
    String testMeDarling(){

        return "oh, test me!";
    }
}

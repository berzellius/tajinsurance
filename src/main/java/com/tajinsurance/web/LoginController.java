package com.tajinsurance.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by berz on 26.03.14.
 */
@RequestMapping("/login")
@Controller
public class LoginController {
    @RequestMapping(produces = "text/html", method = RequestMethod.GET)
    public String getLoginPage(Model uiModel){
        return "login";
    }
}

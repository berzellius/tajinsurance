package com.tajinsurance.web;

import com.tajinsurance.domain.Contract;
import com.tajinsurance.domain.ContractImage;
import com.tajinsurance.service.ContractService;
import com.tajinsurance.utils.CodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Created by berz on 19.09.14.
 */
@Controller
@RequestMapping("/")
public class ImageController {

    @Autowired
    ContractService contractService;

    @Autowired
    CodeUtils codeUtils;

    @RequestMapping(value = "image/{id}", method = RequestMethod.GET, params = "delete")
    String deleteImage(
            @PathVariable("id") Long id,
            HttpServletRequest httpServletRequest
    )
    {
        ContractImage contractImage = contractService.getContractImageById(id);
        Contract contract = contractImage.getContract();
        contractService.deleteImage(contractImage);

        return "redirect:/contracts/" + contract.getId().toString() + "?form";
    }


    @RequestMapping(value = "image/{id}.{ext}", method = RequestMethod.GET)
    void getImage(
            @PathVariable("id") Long id,
            @PathVariable("ext") String extension,
            HttpServletResponse response,
            HttpServletRequest request
    ){
        ContractImage contractImage = contractService.getContractImageById(id);

        File file = null;
        try {
            file = new File(codeUtils.getUploadImagesDirPath() + contractImage.getPath());

            InputStream inputStream = new FileInputStream(file);
            FileCopyUtils.copy(inputStream, response.getOutputStream());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @RequestMapping(value = "image/{id}", params = "edit", method = RequestMethod.GET)
    public String editImage(
            @PathVariable("id") Long id,
            Model uiModel
    )
    {
        ContractImage contractImage = contractService.getContractImageById(id);

        uiModel.addAttribute("image", contractImage);

        return "contracts/image/edit";
    }

    @RequestMapping(value = "image/{id}", method = RequestMethod.POST)
    @ResponseBody
    public String updateImage(
            @PathVariable("id") Long id,
            @RequestParam("description") String description
    ){
        ContractImage contractImage = contractService.getContractImageById(id);
        contractImage.setDescription(description);

        contractService.updateImage(contractImage);

        return "updated";
    }
}

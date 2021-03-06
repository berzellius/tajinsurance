// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.tajinsurance.web;

privileged aspect ContractController_Roo_Controller {
    
    /*@RequestMapping(method = RequestMethod.POST, produces = "text/html")
    public String ContractController.create(@Valid Contract contract, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, contract);
            return "contracts/create";
        }
        uiModel.asMap().clear();
        contract.persist();
        return "redirect:/contracts/" + encodeUrlPathSegment(contract.getId().toString(), httpServletRequest);
    }*/


    
    /*@RequestMapping(params = "form", produces = "text/html")
    public String ContractController.createForm(Model uiModel) {
        populateEditForm(uiModel, new Contract());
        return "contracts/create";
    }*/
    
    /*@RequestMapping(value = "/{id}", produces = "text/html")
    public String ContractController.show(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("contract", contractService.getContractById(id));
        uiModel.addAttribute("itemId", id);
        return "contracts/show";
    }*/

    /*
    @RequestMapping(produces = "text/html")
    public String ContractController.list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "sortFieldName", required = false) String sortFieldName, @RequestParam(value = "sortOrder", required = false) String sortOrder, Model uiModel) {

        List<Contract> contracts = contractService.getContractsPage(page, size, sortFieldName, sortOrder);

        uiModel.addAttribute("contracts", contracts);

        uiModel.addAttribute("maxPages", contractService.countPages(size));

        return "contracts/list";
    }
    */
    /*@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String ContractController.update(@Valid Contract contract, BindingResult bindingResult, Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            populateEditForm(uiModel, contract);
            return "contracts/update";
        }
        uiModel.asMap().clear();
        contract.merge();
        return "redirect:/contracts/" + encodeUrlPathSegment(contract.getId().toString(), httpServletRequest);
    }*/
    /*@RequestMapping(method = RequestMethod.PUT, produces = "text/html")
    public String ContractController.update(Contract contract, Model uiModel, HttpServletRequest httpServletRequest) {
        contractService.update(contract);
        uiModel.asMap().clear();

        return "redirect:/contracts/" + encodeUrlPathSegment(contract.getId().toString(), httpServletRequest);
    }


    @RequestMapping(value = "/{id}", params = "form", produces = "text/html")
    public String ContractController.updateForm(@PathVariable("id") Long id, Model uiModel) {
        populateEditForm(uiModel, contractService.getContractById(id));
        return "contracts/update";
    }
    
    /*@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String ContractController.delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        Contract contract = Contract.findContract(id);
        contract.remove();
        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/contracts";
    }*/
    /*@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "text/html")
    public String ContractController.delete(@PathVariable("id") Long id, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "size", required = false) Integer size, Model uiModel) {
        contractService.delete(id);

        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/contracts";
    }
    
    void ContractController.populateEditForm(Model uiModel, Contract contract) {
        uiModel.addAttribute("contract", contract);
    }
    
    String ContractController.encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        String enc = httpServletRequest.getCharacterEncoding();
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        } catch (UnsupportedEncodingException uee) {}
        return pathSegment;
    }
    */
}

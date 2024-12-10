package com.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    @GetMapping("/error/401")
    public String handleUnauthorized(Model model) {
        model.addAttribute("message", "You are not authorized to access this page.");
        return "error/401";
    }

    @GetMapping("/error/403")
    public String handleForbidden(Model model) {
        model.addAttribute("message", "You do not have permission to access this page.");
        return "error/403";
    }

    @GetMapping("/error/404")
    public String handleNotFound(Model model) {
        model.addAttribute("message", "The requested page was not found.");
        return "error/404";
    }

    @GetMapping("/error/500")
    public String handleServerError(Model model) {
        model.addAttribute("message", "Happened some error on server side. Please try again later.");
        return "error/500";
    }

    @GetMapping("/error/token-expired")
    public String handleTokenExpired(Model model) {
        model.addAttribute("message", "Your token has expired. Please login again.");
        return "error/token-expired";
    }
}

package com.oauth.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MyController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal OAuth2User oAuth2User, Model model) {
        model.addAttribute("name", oAuth2User.getAttribute("name"));
        model.addAttribute("login", oAuth2User.getAttribute("login"));
        model.addAttribute("id", oAuth2User.getAttribute("id"));
        model.addAttribute("email", oAuth2User.getAttribute("email"));
        model.addAttribute("roles", oAuth2User.getAuthorities());
        return "profile";
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "admin";
    }
}

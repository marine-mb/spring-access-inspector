package com.theodo.ms.controller;

import static com.theodo.ms.controller.TreesController.Config.PATH;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@RestController
@RequiredArgsConstructor
@RequestMapping(PATH)
@PreAuthorize("hasRole(ADMIN)")
public class TreesController {

    @PostMapping()
    public String postSomething() {
        return "OK";
    }

    @GetMapping()
    @PreAuthorize("hasRole(USER)")
    public String getSomething() {
        return "OK";
    }

    @UtilityClass
    public static class Config {
        public static final String PATH = "trees";
    }
}

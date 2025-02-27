package com.theodo.ms.controller;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.theodo.ms.controller.TreesController.Config.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(PATH)
public class TreesController {

    @PostMapping()
    public String postSomething() {
        return "OK";
    }

    @UtilityClass
    public static class Config {
        public static final String PATH = "trees";
    }
}

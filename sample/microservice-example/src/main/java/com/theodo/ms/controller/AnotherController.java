package com.theodo.ms.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@RestController
@RequestMapping(AnotherController.Config.ANOTHER_PATH)
@RequiredArgsConstructor
public class AnotherController {

    protected static final String UPLOAD = "/upload";

    @PutMapping("{projectId}" + UPLOAD)
    @Secured("ROLE_USER")
    public String postSomething(@PathVariable String projectId) {
        return "OK: %s".formatted(projectId);
    }

    @DeleteMapping(AnotherController.AnotherConfig.DELETE + "/{projectId}/{info}")
    @Secured("ROLE_ADMIN")
    public String getSomething(@PathVariable String projectId, @PathVariable Integer info) {
        return "OK: %s %s".formatted(projectId, info);
    }

    @RequestMapping(value = "{projectId}" + AnotherController.Config.MODIFY, method = RequestMethod.PATCH)
    public String pathSomething(@PathVariable String projectId) {
        return "OK: %s".formatted(projectId);
    }

    @UtilityClass
    public static class Config {
        public static final String ANOTHER_PATH = "another";
        public static final String MODIFY = "/modify";
    }

    protected static class AnotherConfig {
        private static final String DELETE = "/delete";
    }
}

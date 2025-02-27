package com.theodo.ms.controller;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AnotherController.Config.ANOTHER_PATH)
@RequiredArgsConstructor
public class AnotherController {

    protected static final String UPLOAD = "/upload";

    @PutMapping("{projectId}" + UPLOAD)
    public String postSomething(@PathVariable String projectId) {
        return "OK: %s".formatted(projectId);
    }

    @DeleteMapping(AnotherController.AnotherConfig.DELETE + "/{projectId}/{info}")
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
        private static final String  DELETE = "/delete";
    }
}

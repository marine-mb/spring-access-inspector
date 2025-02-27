package com.theodo.ms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class HousesController {

    @PostMapping({"/houses/v1/house"})
    public String postSomething() {
        return "OK";
    }

}

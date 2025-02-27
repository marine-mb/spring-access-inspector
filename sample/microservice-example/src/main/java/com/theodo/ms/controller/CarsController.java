package com.theodo.ms.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("webclient")
@RequiredArgsConstructor
public class CarsController {

    @PostMapping({ "/v1/cars" })
    public String postSomething() {
        return "OK";
    }

    @GetMapping({ "/v2/cars" })
    public String getSomethingWithQueryParam(@RequestParam String carsId) {
        return "OK: %s".formatted(carsId);
    }

    @GetMapping({ "/v3/cars/{carsId}/details" })
    public String getSomething(@PathVariable String carsId) {
        return "OK: %s".formatted(carsId);
    }

    @GetMapping(path = "/v4/cars/{carsId}/{info}")
    public String getSomething(@PathVariable String carsId, @PathVariable Integer info) {
        return "OK: %s %s".formatted(carsId, info);
    }

    @PatchMapping(path = "/v5/cars")
    public String patchAllCars() {
        return "OK";
    }
}

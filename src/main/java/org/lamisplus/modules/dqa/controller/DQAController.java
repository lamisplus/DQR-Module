package org.lamisplus.modules.dqa.controller;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.lamisplus.modules.dqa.service.DQAService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class DQAController {
    private final DQAService starterService;

    @GetMapping("/starter")
    public String getEnrollment() {
        return "get starter module";
    }

//    @PostMapping("/starter")
//    public String createEnrollment(@RequestBody StarterDTO starterDTO) {
//        return null;
//    }
}

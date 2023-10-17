package org.lamisplus.modules.starter.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.starter.domain.dto.request.StarterDTO;
import org.lamisplus.modules.starter.service.StarterService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class StarterController {
    private final StarterService starterService;

    @GetMapping("/starter")
    public String getEnrollment() {
        return "get starter module";
    }

    @PostMapping("/starter")
    public String createEnrollment(@RequestBody StarterDTO starterDTO) {
        return null;
    }
}

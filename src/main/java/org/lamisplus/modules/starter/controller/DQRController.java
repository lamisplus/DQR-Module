package org.lamisplus.modules.starter.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lamisplus.modules.starter.domain.dto.request.DQRDTO;
import org.lamisplus.modules.starter.service.DQRService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class DQRController {
    private final DQRService starterService;

    @GetMapping("/dqr")
    public String getEnrollment() {
        return "get starter module";
    }

    @PostMapping("/dqr")
    public String createEnrollment(@RequestBody DQRDTO starterDTO) {
        return null;
    }
}

package org.lamisplus.modules.dqr.service.cbs;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.dqr.domain.cbs.CbsSettings;
import org.lamisplus.modules.dqr.domain.cbs.entity.CbsSetting;
import org.lamisplus.modules.dqr.repository.cbs.CbsSettingRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CbsSettingsService {

    private final CbsSettingRepository repository;

    public CbsSettings current() {
        Map<String, String> values = new HashMap<>();
        repository.findAll().forEach(s -> values.put(s.getSettingKey(), s.getSettingValue()));
        return CbsSettings.from(values);
    }

    public List<CbsSetting> all() {
        return repository.findAllByOrderBySettingKey();
    }
}

package org.lamisplus.modules.dqr.domain.cbs.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "cbs_setting")
public class CbsSetting {
    @Id
    private String settingKey;
    private String settingValue;
    private String description;
}

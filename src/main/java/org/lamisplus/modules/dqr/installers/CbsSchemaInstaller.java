package org.lamisplus.modules.dqr.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.AcrossLiquibaseInstaller;
import org.springframework.core.annotation.Order;

// Bump the version whenever a new changeSet is added to the changelog,
// otherwise Across skips the installer on the next start.
@Order(1)
@Installer(name = "dqr-cbs-schema",
        description = "Creates the HIV Case-Based Surveillance monitoring tables",
        version = 3)  // 2: CBS-DQ-01..28 settings, 3: cbs_finding.event_date (17 Nov 2026)
public class CbsSchemaInstaller extends AcrossLiquibaseInstaller {
    public CbsSchemaInstaller() {
        super("classpath:installers/dqr/cbs/cbs-schema-1.xml");
    }
}

package org.lamisplus.modules.dqr.domain.cbs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CbsEventType {
    HIV_DIAGNOSIS("HIV Diagnosis", true),
    LINKAGE_TO_CARE("Linkage to Care", true),
    ART_INITIATION("ART Initiation", true),
    TRANSFER_IN("Transfer In", true),
    TRANSFER_OUT("Transfer Out", true),
    TREATMENT_INTERRUPTION("Treatment Interruption", true),
    RETURN_TO_CARE("Return to Care", true),
    DEATH("Death", true),
    // Shown on the timeline as supporting information only. The spec keeps lab results
    // and EAC out of the core CBS milestones, but the patient tracker mockup displays them.
    VIRAL_LOAD("Viral Load Test", false),
    EAC_SESSION("EAC Session", false),
    BASELINE_CD4("Baseline CD4 / AHD", false),
    POST_EAC_VIRAL_LOAD("Post-EAC Viral Load", false),
    REGIMEN_SWITCH("Regimen Line Switch", false);

    private final String label;
    private final boolean primary;
}

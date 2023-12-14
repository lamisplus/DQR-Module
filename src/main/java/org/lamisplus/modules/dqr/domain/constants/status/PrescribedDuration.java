package org.lamisplus.modules.dqr.domain.constants.status;

public enum PrescribedDuration {
    EIGHT(8),
    TWELVE(12),
    TWENTY_FOUR(24);

    private final int value;

    PrescribedDuration(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

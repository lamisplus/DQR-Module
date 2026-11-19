package org.lamisplus.modules.dqr.domain.cbs.dto;

import lombok.Data;

/** Paging and sorting shared by the list filters. Out-of-range values are pulled back into range. */
@Data
public abstract class CbsPageFilter {
    private static final int MAX_PAGE_SIZE = 200;

    private int page = 0;
    private int size = 10;
    private String sort;
    private String direction;

    public int getPage() {
        return Math.max(page, 0);
    }

    public int getSize() {
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    }
}

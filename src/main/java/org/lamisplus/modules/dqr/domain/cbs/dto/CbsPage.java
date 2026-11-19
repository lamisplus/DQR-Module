package org.lamisplus.modules.dqr.domain.cbs.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/** One page of a list, in the shape the MaterialTable screens read. */
@Data
@NoArgsConstructor
public class CbsPage<T> {
    private List<T> content;
    private long totalElements;
    private int page;
    private int size;

    public CbsPage(Page<T> page) {
        this.content = page.getContent();
        this.totalElements = page.getTotalElements();
        this.page = page.getNumber();
        this.size = page.getSize();
    }
}

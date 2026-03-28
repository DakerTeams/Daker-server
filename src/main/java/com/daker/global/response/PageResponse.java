package com.daker.global.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResponse<T> {

    private final List<T> items;
    private final long total;
    private final int page;
    private final int limit;
    private final boolean hasNext;

    public PageResponse(Page<T> page) {
        this.items = page.getContent();
        this.total = page.getTotalElements();
        this.page = page.getNumber() + 1;
        this.limit = page.getSize();
        this.hasNext = page.hasNext();
    }
}

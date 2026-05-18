package com.agentedu.common;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PageResult<T> {

    private Long total;

    private Long pages;

    private List<T> records;

    public PageResult(Long total, List<T> records) {
        this(total, null, records);
    }

    public PageResult(Long total, Long pages, List<T> records) {
        this.total = total;
        this.pages = pages;
        this.records = records;
    }
}

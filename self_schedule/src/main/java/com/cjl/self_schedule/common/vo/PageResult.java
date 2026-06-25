package com.cjl.self_schedule.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private List<T> data;
    private Integer pageNum;
    private Integer pageSize;
    private Long total;
    private Integer pages;
    
    private List<T> records;
    private Long current;
    private Long size;
    private Boolean hasNext;
    private Boolean hasPrevious;
    
    public static <T> PageResult<T> of(List<T> data, Integer pageNum, Integer pageSize, Long total) {
        PageResult<T> result = new PageResult<>();
        result.setData(data);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotal(total);
        result.setPages((int) Math.ceil((double) total / pageSize));
        result.setRecords(data);
        result.setCurrent(pageNum.longValue());
        result.setSize(pageSize.longValue());
        result.setHasNext(pageNum < result.getPages());
        result.setHasPrevious(pageNum > 1);
        return result;
    }
}
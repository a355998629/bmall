package com.bw.bmall.publisher.mapper;

import com.bw.bmall.publisher.bean.HourBean;

import java.util.List;
import java.util.Map;

public interface DauMapper {
    /**
     * DauMapper
     * com.bw.bmall.publisher.mapper.DauMapper
     * 查询每天到现在为止的日活总数
     * @param date
     * @return
     */
    public Long selectDauTotal(String date);

    /**
     * 查询每天每个小时的活跃用户数
     * @param date
     * @return
     */
    public List<Map> selectDauTotalHours(String date);
}

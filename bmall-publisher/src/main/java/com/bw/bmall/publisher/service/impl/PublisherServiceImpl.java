package com.bw.bmall.publisher.service.impl;

import com.bw.bmall.publisher.bean.HourBean;
import com.bw.bmall.publisher.mapper.DauMapper;
import com.bw.bmall.publisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PublisherServiceImpl implements PublisherService {
    @Autowired(required=false)
    private DauMapper dauMapper;
    @Override
    public Long getDauTotal(String date) {
        return dauMapper.selectDauTotal(date);
    }

    @Override
    public Map getDauTotalHour(String date) {
        List<Map> hourListMap = dauMapper.selectDauTotalHours(date);
        Map hourMap = new HashMap();
        for (Map map:hourListMap) {
            hourMap.put(map.get("hours"),map.get("cts"));
        }
        return hourMap;
    }
}

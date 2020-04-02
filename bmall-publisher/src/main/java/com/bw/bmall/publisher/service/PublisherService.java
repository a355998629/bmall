package com.bw.bmall.publisher.service;

import com.bw.bmall.publisher.bean.HourBean;

import java.util.List;
import java.util.Map;

public interface PublisherService {
    public Long getDauTotal(String date);

    public Map getDauTotalHour(String date);
}

package com.bw.bmall.publisher.controller;

import com.alibaba.fastjson.JSON;
import com.bw.bmall.publisher.bean.HourBean;
import com.bw.bmall.publisher.service.PublisherService;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class PublisherController {
    @Autowired(required=false)
    private PublisherService publisherService;
    @GetMapping("realtime-total")
    @ResponseBody
    public String realtimeDate(@RequestParam("date") String date){
        List<Map> list = new ArrayList<Map>();
        //日活总数
        long dautotal = publisherService.getDauTotal(date);
        Map dauMap = new HashMap<String,Object>();
        dauMap.put("id","dau");
        dauMap.put("name","活跃用户数");
        dauMap.put("value",dautotal);
        list.add(dauMap);

        return JSON.toJSONString(list);
    }

    @GetMapping("realtime-hour")
    @ResponseBody
    public String realtimeDateHour(@RequestParam("date") String date,@RequestParam("id") String id){
        if (id.equals("dau")){
            Map dauHourMapTD = publisherService.getDauTotalHour(date);
            String yd = getYD(date);
            Map dauHourMapYD = publisherService.getDauTotalHour(yd);

            Map hourMap = new HashMap();
            hourMap.put("today",dauHourMapTD);
            hourMap.put("yesterday",dauHourMapYD);
            return JSON.toJSONString(hourMap);
        }else {
            return "";
        }
    }
    private String getYD(String td){
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date today = sf.parse(td);
            Date yesterday = DateUtils.addDays(today, -1);
            return sf.format(yesterday);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
}

package com.zlg.scheduler.controller;

import com.zlg.scheduler.controller.model.ApiBaseResp;
import com.zlg.scheduler.service.SchedulerService;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Api(tags = "pressure")
public class SchedulerController implements PressureApi{

    @Resource
    private SchedulerService service;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public ResponseEntity<ApiBaseResp> pressureStart(Integer deviceNumber, String deviceType, Integer part, Integer rest, Integer period, String topic, String data) {

        logger.info("调度器开始分配任务");
        service.pressureStart(deviceNumber, deviceType, part, rest, period, topic, data);



        return ResponseEntity.ok(new ApiBaseResp().message("success"));
    }

    @Override
    public ResponseEntity<ApiBaseResp> pressureStop() {
        return null;
    }
}

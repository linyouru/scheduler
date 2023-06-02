package com.zlg.scheduler.controller;

import com.zlg.scheduler.controller.model.ApiBaseResp;
import com.zlg.scheduler.controller.model.ApiSandData;
import com.zlg.scheduler.exception.BizException;
import com.zlg.scheduler.service.SchedulerService;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Api(tags = "pressure")
public class SchedulerController implements PressureApi {

    @Resource
    private SchedulerService service;
    private boolean underPressure = false;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public ResponseEntity<ApiBaseResp> deviceOnline(Integer deviceNumber, String deviceType, Integer part, Integer rest) {
        if (underPressure) {
            throw new BizException(HttpStatus.BAD_REQUEST, "pressure.1002");
        }
        logger.debug("调度器触发设备上线");
        try {
            service.deviceOnline(deviceNumber, deviceType, part, rest);
        } catch (Exception e) {
            logger.error("调度器调用deviceOnline异常: {}", e.getMessage());
        }
        underPressure = true;
        return ResponseEntity.ok(new ApiBaseResp().message("success"));
    }

    @Override
    public ResponseEntity<ApiBaseResp> pressureStart(ApiSandData body, Integer deviceNumber, String deviceType, Integer part, Integer rest, Integer period, String topic) {
        if (underPressure) {
            throw new BizException(HttpStatus.BAD_REQUEST, "pressure.1002");
        }
        logger.debug("调度器触发开始压测");
        try {
            service.pressureStart(deviceNumber, deviceType, part, rest, period, topic, body.getData());
        } catch (Exception e) {
            logger.error("调度器调用pressureStart异常: {}", e.getMessage());
        }
        underPressure = true;

        return ResponseEntity.ok(new ApiBaseResp().message("success"));
    }

    @Override
    public ResponseEntity<ApiBaseResp> pressureStop() {
        logger.debug("调度器触发停止压测");
        try {
            service.pressureStop();
        } catch (Exception e) {
            logger.error("调度器调用pressureStop异常: {}", e.getMessage());
        }
        underPressure = false;
        return ResponseEntity.ok(new ApiBaseResp().message("success"));
    }
}

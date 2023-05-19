package com.zlg.scheduler.service;

import com.zlg.scheduler.controller.model.ApiBaseResp;
import com.zlg.scheduler.exception.BizException;
import com.zlg.scheduler.pojo.ExecuteScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Service
public class SchedulerService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final static WebClient WEB_CLIENT;
    @Value(value = "${execute}")
    private String executeHosts;

    static {
        WEB_CLIENT = WebClient.builder().codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024)).build();
    }

    public void pressureStart(Integer deviceNumber, String deviceType, Integer part, Integer rest, Integer period, String topic, String data) {

        String[] executeHostList = executeHosts.split(",");
//        int executeTotal = executeHostList.length;
        int executeTotal = part;

        if (deviceNumber < 1 || deviceNumber > 500000) {
            throw new BizException(HttpStatus.BAD_REQUEST, "pressure.1001");
        }

        if (deviceNumber < 50) {
            //全给一个执行器
            ExecuteScope executeScope = new ExecuteScope();
            executeScope.setDeviceTotal(deviceNumber);
            executeScope.setStartUser(1);
            executeScope.setStartDevice(1);
            //调用执行器接口
            Mono<ApiBaseResp> baseRespMono = WEB_CLIENT.get()
                    .uri("http://192.168.24.91/v1/pressure/start?")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(ApiBaseResp.class);
            ApiBaseResp block = baseRespMono.block();
            logger.info("调用执行器压测接口结果:{}",block.getMessage());
        } else {
            ArrayList<ExecuteScope> executeScopes = new ArrayList<>();
            int excess = deviceNumber % executeTotal;
            int bucket = (deviceNumber - excess) / executeTotal;
            int total = 0;
            int currentUser = 1;
            int currentDevice = 1;
            one:
            for (int i = 1; i <= 1000; i++) {
                if ("invert".equals(deviceType)) {
                    for (int j = 1; j <= 500; j++) {
                        total++;
                        if (total >= bucket && total % bucket == 0) {
                            ExecuteScope executeScope = new ExecuteScope();
                            executeScope.setStartUser(currentUser);
                            executeScope.setStartDevice(currentDevice);
                            if (executeScopes.size() == executeTotal - 1) {
                                executeScope.setDeviceTotal(bucket + excess);
                            } else {
                                executeScope.setDeviceTotal(bucket);
                            }
                            executeScopes.add(executeScope);
                            currentUser = j + 1 > 500 ? i + 1 : i;
                            currentDevice = j + 1 > 500 ? 1 : j + 1;
                        }
                        if (total == deviceNumber) {
                            break one;
                        }
                    }
                } else if ("can-common".equals(deviceType)) {
                    for (int j = 1; j <= 2; j++) {
                        total++;
                        if (total >= bucket && total % bucket == 0) {
                            ExecuteScope executeScope = new ExecuteScope();
                            executeScope.setStartUser(currentUser);
                            executeScope.setStartDevice(currentDevice);
                            if (executeScopes.size() == executeTotal - 1) {
                                executeScope.setDeviceTotal(bucket + excess);
                            } else {
                                executeScope.setDeviceTotal(bucket);
                            }
                            executeScopes.add(executeScope);
                            currentUser = j + 1 > 2 ? i + 1 : i;
                            currentDevice = j + 1 > 2 ? 1 : j + 1;
                        }
                        if (total == deviceNumber) {
                            break one;
                        }
                    }
                }
            }
            logger.info(executeScopes.toString());
            //分好每个执行器的设备范围，调用执行器接口


        }


    }


}

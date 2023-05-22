package com.zlg.scheduler.service;

import com.zlg.scheduler.controller.model.ApiBaseResp;
import com.zlg.scheduler.exception.BizException;
import com.zlg.scheduler.pojo.ExecuteScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
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

    public void deviceOnline(Integer deviceNumber, String deviceType, Integer part, Integer rest) {

        String[] executeHostList = executeHosts.split(",");
        int executeTotal = executeHostList.length;
        if (deviceNumber < 1 || deviceNumber > 500000) {
            throw new BizException(HttpStatus.BAD_REQUEST, "pressure.1001");
        }
        if (deviceNumber <= executeTotal) {
            //全给一个执行器,调用执行器接口
            requestDeviceOnline(executeHostList[0], deviceNumber, deviceType, part, rest, 1, 1);
        } else {
            ArrayList<ExecuteScope> executeScopes = getExecuteScopes(deviceNumber, deviceType, executeTotal);
            //分好每个执行器的设备范围，调用执行器接口
            for (int i = 0; i < executeHostList.length; i++) {
                ExecuteScope executeScope = executeScopes.get(i);
                requestDeviceOnline(executeHostList[i], executeScope.deviceTotal, deviceType, part, rest, executeScope.startUser, executeScope.startDevice);
            }
        }
    }

    public void pressureStart(Integer deviceNumber, String deviceType, Integer part, Integer rest, Integer period, String topic, String data) {

        String[] executeHostList = executeHosts.split(",");
        int executeTotal = executeHostList.length;
        if (deviceNumber < 1 || deviceNumber > 500000) {
            throw new BizException(HttpStatus.BAD_REQUEST, "pressure.1001");
        }
        if (deviceNumber <= executeTotal) {
            //全给一个执行器,调用执行器接口
            requestPressureStart(executeHostList[0], deviceNumber, deviceType, part, rest, period, topic, data, 1, 1);
        } else {
            ArrayList<ExecuteScope> executeScopes = getExecuteScopes(deviceNumber, deviceType, executeTotal);
            //分好每个执行器的设备范围，调用执行器接口
            for (int i = 0; i < executeHostList.length; i++) {
                ExecuteScope executeScope = executeScopes.get(i);
                requestPressureStart(executeHostList[i], executeScope.deviceTotal, deviceType, part, rest, period, topic, data, executeScope.startUser, executeScope.startDevice);
            }
        }
    }

    public void pressureStop() {
        String[] executeHostList = executeHosts.split(",");
        for (String executeHost : executeHostList) {
            requestPressureStop(executeHost);
        }
    }

    private ArrayList<ExecuteScope> getExecuteScopes(Integer deviceNumber, String deviceType, int executeTotal) {
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
                            executeScopes.add(executeScope);
                            break one;
                        }
                        executeScope.setDeviceTotal(bucket);
                        executeScopes.add(executeScope);
                        currentUser = j + 1 > 500 ? i + 1 : i;
                        currentDevice = j + 1 > 500 ? 1 : j + 1;
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
                            executeScopes.add(executeScope);
                            break one;
                        }
                        executeScope.setDeviceTotal(bucket);
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
        return executeScopes;
    }

    private void requestDeviceOnline(String host, Integer deviceNumber, String deviceType, Integer part, Integer rest, Integer startUserIndex, Integer startDeviceIndex) {
        String uri = new StringBuilder("http://")
                .append(host)
                .append("/v1/pressure/device_onlien?")
                .append("deviceNumber=").append(deviceNumber).append("&")
                .append("deviceType=").append(deviceType).append("&")
                .append("part=").append(part).append("&")
                .append("rest=").append(rest).append("&")
                .append("startUserIndex=").append(startUserIndex).append("&")
                .append("startDeviceIndex=").append(startDeviceIndex)
                .toString();
        WebClient.RequestBodyUriSpec bodyUriSpec = WEB_CLIENT.method(HttpMethod.GET);
        Mono<ApiBaseResp> baseRespMono = WEB_CLIENT.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ApiBaseResp.class);
        ApiBaseResp block = baseRespMono.block();
    }

    private void requestPressureStart(String host, Integer deviceNumber, String deviceType, Integer part, Integer rest, Integer period, String topic, String data, Integer startUserIndex, Integer startDeviceIndex) {
        String uri = new StringBuilder("http://")
                .append(host)
                .append("/v1/pressure/start?")
                .append("deviceNumber=").append(deviceNumber).append("&")
                .append("deviceType=").append(deviceType).append("&")
                .append("part=").append(part).append("&")
                .append("rest=").append(rest).append("&")
                .append("period=").append(period).append("&")
                .append("topic=").append(topic).append("&")
                .append("data=").append(data).append("&")
                .append("startUserIndex=").append(startUserIndex).append("&")
                .append("startDeviceIndex=").append(startDeviceIndex)
                .toString();
        Mono<ApiBaseResp> baseRespMono = WEB_CLIENT.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ApiBaseResp.class);
        ApiBaseResp block = baseRespMono.block();
    }

    private void requestPressureStop(String host) {
        String uri = new StringBuilder("http://").append(host) .append("/v1/pressure/stop").toString();
        Mono<ApiBaseResp> baseRespMono = WEB_CLIENT.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ApiBaseResp.class);
        ApiBaseResp block = baseRespMono.block();
    }

}

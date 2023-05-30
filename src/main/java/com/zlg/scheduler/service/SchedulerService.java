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
    private final static int TEST_TENANT_TOTAL = 1000;
    private final static int INVERT_TOTAL = 500;
    private final static int CAN_COMMON_TOTAL = 2;
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

    /**
     * 计算每个执行器压测设备范围
     *
     * @param deviceNumber 压测设备总数
     * @param deviceType   设备类型
     * @param executeTotal 执行器实例总数
     * @return 每个执行器压测设备的范围
     */
    private ArrayList<ExecuteScope> getExecuteScopes(Integer deviceNumber, String deviceType, int executeTotal) {
        ArrayList<ExecuteScope> executeScopes = new ArrayList<>();
        int excess = deviceNumber % executeTotal;
        int bucket = (deviceNumber - excess) / executeTotal;
        int total = 0;
        int currentUser = 1;
        int currentDevice = 1;
        one:
        for (int i = 1; i <= TEST_TENANT_TOTAL; i++) {
            if ("invert" .equals(deviceType)) {
                for (int j = 1; j <= INVERT_TOTAL; j++) {
                    total++;
                    if (total >= bucket && total % bucket == 0) {
                        if (calculateScope(executeTotal, executeScopes, excess, bucket, currentUser, currentDevice))
                            break;
                        currentUser = j + 1 > INVERT_TOTAL ? i + 1 : i;
                        currentDevice = j + 1 > INVERT_TOTAL ? 1 : j + 1;
                    }
                }
            } else if ("can-common" .equals(deviceType)) {
                for (int j = 1; j <= CAN_COMMON_TOTAL; j++) {
                    total++;
                    if (total >= bucket && total % bucket == 0) {
                        if (calculateScope(executeTotal, executeScopes, excess, bucket, currentUser, currentDevice))
                            break one;
                        currentUser = j + 1 > CAN_COMMON_TOTAL ? i + 1 : i;
                        currentDevice = j + 1 > CAN_COMMON_TOTAL ? 1 : j + 1;
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

    /**
     * 设置每个执行器压测的设备范围,并添加到范围List中
     *
     * @param executeTotal  执行器总数
     * @param executeScopes 设备范围List
     * @param excess        设备余数
     * @param bucket        每个执行器均分到的设备数
     * @param currentUser   当前用户
     * @param currentDevice 当前设备
     * @return 是否已经计算到最后一个执行器的范围
     */
    private boolean calculateScope(int executeTotal, ArrayList<ExecuteScope> executeScopes, int excess, int bucket, int currentUser, int currentDevice) {
        ExecuteScope executeScope = new ExecuteScope();
        executeScope.setStartUser(currentUser);
        executeScope.setStartDevice(currentDevice);
        if (executeScopes.size() == executeTotal - 1) {
            //最后一个执行器的压测设备范围
            executeScope.setDeviceTotal(bucket + excess);
            executeScopes.add(executeScope);
            return true;
        }
        executeScope.setDeviceTotal(bucket);
        executeScopes.add(executeScope);
        return false;
    }

    /**
     * 调用执行器设备上线接口
     *
     * @param host             执行器host
     * @param deviceNumber     执行器压测设备数
     * @param deviceType       设备类型invert,can-common
     * @param part             每批上线多少设备
     * @param rest             两批设备上线间隔(ms)
     * @param startUserIndex   执行器组装设备信息时起始用户
     * @param startDeviceIndex 执行器组装设备信息时起始设备
     */
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
        Mono<ApiBaseResp> baseRespMono = WEB_CLIENT.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ApiBaseResp.class);
        ApiBaseResp block = baseRespMono.block();
    }

    /**
     * 调用执行器开始压测接口
     *
     * @param host             执行器host
     * @param deviceNumber     执行器压测设备数
     * @param deviceType       设备类型invert,can-common
     * @param part             每批上线多少设备
     * @param rest             两批设备上线间隔(ms)
     * @param period           上报数据的周期(s);设备将在此周期内尽量均匀上报数据
     * @param topic            上报数据类型，data、raw
     * @param data             上报的数据，为base64字符串
     * @param startUserIndex   执行器组装设备信息时起始用户
     * @param startDeviceIndex 执行器组装设备信息时起始设备
     */
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

    /**
     * 调用执行器停止压测接口
     *
     * @param host 执行器host
     */
    private void requestPressureStop(String host) {
        String uri = new StringBuilder("http://").append(host).append("/v1/pressure/stop").toString();
        Mono<ApiBaseResp> baseRespMono = WEB_CLIENT.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ApiBaseResp.class);
        ApiBaseResp block = baseRespMono.block();
    }

}

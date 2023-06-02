package com.zlg.scheduler.service;

import com.zlg.scheduler.controller.model.ApiBaseResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AsyncSchedulerService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final static WebClient WEB_CLIENT;

    static {
        WEB_CLIENT = WebClient.builder().codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(5 * 1024 * 1024)).build();
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
    @Async
    public void requestDeviceOnline(String host, Integer deviceNumber, String deviceType, Integer part, Integer rest, Integer startUserIndex, Integer startDeviceIndex) {
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
        logger.debug("调度器调用: {}", uri);
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
    @Async
    public void requestPressureStart(String host, Integer deviceNumber, String deviceType, Integer part, Integer rest, Integer period, String topic, String data, Integer startUserIndex, Integer startDeviceIndex) {
        String parentsJson = "{\"data\":\"" + data + "\"}";

        String uri = new StringBuilder("http://")
                .append(host)
                .append("/v1/pressure/start?")
                .append("deviceNumber=").append(deviceNumber).append("&")
                .append("deviceType=").append(deviceType).append("&")
                .append("part=").append(part).append("&")
                .append("rest=").append(rest).append("&")
                .append("period=").append(period).append("&")
                .append("topic=").append(topic).append("&")
                .append("startUserIndex=").append(startUserIndex).append("&")
                .append("startDeviceIndex=").append(startDeviceIndex)
                .toString();
        logger.debug("调度器调用: {}", uri);
        Mono<ApiBaseResp> baseRespMono = WEB_CLIENT.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(parentsJson)
                .retrieve()
                .bodyToMono(ApiBaseResp.class);
        ApiBaseResp block = baseRespMono.block();
    }

    /**
     * 调用执行器停止压测接口
     *
     * @param host 执行器host
     */
    @Async
    public void requestPressureStop(String host) {
        String uri = new StringBuilder("http://").append(host).append("/v1/pressure/stop").toString();
        logger.debug("调度器调用: {}", uri);
        Mono<ApiBaseResp> baseRespMono = WEB_CLIENT.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ApiBaseResp.class);
        ApiBaseResp block = baseRespMono.block();
    }
}

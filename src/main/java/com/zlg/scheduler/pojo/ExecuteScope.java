package com.zlg.scheduler.pojo;

public class ExecuteScope {

    public Integer startUser;
    public Integer startDevice;
    public Integer deviceTotal;

    public Integer getDeviceTotal() {
        return deviceTotal;
    }

    public void setDeviceTotal(Integer deviceTotal) {
        this.deviceTotal = deviceTotal;
    }

    public Integer getStartUser() {
        return startUser;
    }

    public void setStartUser(Integer startUser) {
        this.startUser = startUser;
    }

    public Integer getStartDevice() {
        return startDevice;
    }

    public void setStartDevice(Integer startDevice) {
        this.startDevice = startDevice;
    }


    @Override
    public String toString() {
        return "ExecuteScope{" +
                "startUser=" + startUser +
                ", startDevice=" + startDevice +
                ", deviceTotal=" + deviceTotal +
                '}';
    }
}

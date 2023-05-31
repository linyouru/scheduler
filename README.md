# 压测调度器

## 部署
1.运行docker目录下的build.sh脚本，生成镜像
2.将镜像推送到ops.zlgcloud.com的docker仓库
```
docker tag zws-r2-pressure-scheduler:latest ops.zlgcloud.com/library/zws-r2-pressure-scheduler:master
docker push ops.zlgcloud.com/library/zws-r2-pressure-scheduler:master
```
在待部署的服务器拉取镜像:  
`docker pull ops.zlgcloud.com/library/zws-r2-pressure-scheduler:master`  
(若未登录docker请先登录docker login -u zycloud -p ZhiyuanCloud1234! ops.zlgcloud.com)

将外部配置文件放到
`/data/pressure2/scheduler/config`

生成容器:
```
docker run -p 9310:9310 --name pressure_scheduler --net=host -v /data/pressure2/scheduler/logs:/home/pressure2scheduler/logs/scheduler -v /data/pressure2/scheduler/config:/home/pressure2scheduler/config -itd ops.zlgcloud.com/library/zws-r2-pressure-execute:master
```

## 给mqtt服务器添加压测节点虚拟网卡的路由
```
sudo route add -net 192.168.10.0/24 gw 192.168.24.95
sudo route add -net 192.168.11.0/24 gw 192.168.24.94
sudo route add -net 192.168.12.0/24 gw 192.168.24.93
sudo route add -net 192.168.13.0/24 gw 192.168.24.92
sudo route add -net 192.168.14.0/24 gw 192.168.24.91
```
压测节点虚拟网卡：
192.168.10.x (24.95)
192.168.11.x (24.94)
192.168.12.x (24.93)
192.168.13.x (24.92)
192.168.14.x (24.91)

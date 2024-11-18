本项目是想帮助学校内做家教的同学而开发的算是创业项目，顺便当作自己的第一个比较大的项目用作练手

虚拟机配置:

本地虚拟机两台 ip地址本人设为192.168.101.68



虚拟机里的环境配置:

docker

xxl-job：2.3.1 

minio：release2022

redis：6.2.7

elasticsearch：7.12.1

gogs

rabbitmq：3.8.34

nacos：1.4.1

mysql：8.0.25



启动步骤：

编写docker的脚本：https://blog.csdn.net/wxuzero/article/details/80610237?fromshare=blogdetail&sharetype=blogdetail&sharerId=80610237&sharerefer=PC&sharesource=m0_62314761&sharefrom=from_link

1.先启动虚拟机，打开finalshell输入以下代码，通过docker启动各个软件

2.systemctl start docker

3.sh /data/soft/restart.sh

# 1、配置redis的主从复制，Cluster集群

### config

[redis-cluster](redis-cluster)


### 启动
``` 
  781  /opt/app/redis/src/redis-server /usr/local/redis-cluster/800*/redis.conf
  782  /opt/app/redis/src/redis-server /usr/local/redis-cluster/8001/redis.conf
  783  /opt/app/redis/src/redis-server /usr/local/redis-cluster/8002/redis.conf
  784  /opt/app/redis/src/redis-server /usr/local/redis-cluster/8003/redis.conf
  785  /opt/app/redis/src/redis-server /usr/local/redis-cluster/8004/redis.conf
  786  /opt/app/redis/src/redis-server /usr/local/redis-cluster/8005/redis.conf
  787  /opt/app/redis/src/redis-server /usr/local/redis-cluster/8006/redis.conf
```
### 配置集群
```
/opt/app/redis/src/redis-cli --cluster create --cluster-replicas 1 172.21.162.130:8001 172.21.162.130:8002 172.21.162.130:8003 172.21.162.130:8004 172.21.162.130:8005 172.21.162.130:8006
```

### 验证集群

``` 
root@izhengyin-vpc:~# redis-cli -c -h 172.21.162.130 -p 8001

172.21.162.130:8001> cluster info

cluster_state:ok
cluster_slots_assigned:16384
cluster_slots_ok:16384
cluster_slots_pfail:0
cluster_slots_fail:0
cluster_known_nodes:6
cluster_size:3
cluster_current_epoch:6
cluster_my_epoch:1
cluster_stats_messages_ping_sent:124
cluster_stats_messages_pong_sent:126
cluster_stats_messages_sent:250
cluster_stats_messages_ping_received:121
cluster_stats_messages_pong_received:124
cluster_stats_messages_meet_received:5
cluster_stats_messages_received:250
172.21.162.130:8001> cluster nodes
973c883b4efaba14d6a0f60a92a4594121518372 172.21.162.130:8006@18006 slave 3d135fe920e05ed281382b794d4262e09db675a2 0 1609895334146 6 connected
64e0e2e977fae56cf475ba97445fe8556de472de 172.21.162.130:8005@18005 slave 3c5ef1fe119cdbd2fc277a58c64722e848380dbc 0 1609895335148 5 connected
ba9359f3792e2db6a0f34c203659b7e6ef9cd565 172.21.162.130:8004@18004 slave 93730763d1a89a9b8502bbe0ead0e8c2b20a2f8d 0 1609895333043 4 connected
3d135fe920e05ed281382b794d4262e09db675a2 172.21.162.130:8002@18002 master - 0 1609895334546 2 connected 5461-10922
93730763d1a89a9b8502bbe0ead0e8c2b20a2f8d 172.21.162.130:8003@18003 master - 0 1609895333144 3 connected 10923-16383
3c5ef1fe119cdbd2fc277a58c64722e848380dbc 172.21.162.130:8001@18001 myself,master - 0 1609895334000 1 connected 0-5460
172.21.162.130:8001>
172.21.162.130:8001>
172.21.162.130:8001> set testkey 1

root@izhengyin-vpc:~# redis-cli -c -h 172.21.162.130 -p 8002
172.21.162.130:8002> get testkey
-> Redirected to slot [4757] located at 172.21.162.130:8001
"1"
172.21.162.130:8001>
```
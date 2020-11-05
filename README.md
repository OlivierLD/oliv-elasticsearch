# Elasticsearch
Get started, hints, stuff, ideas, etc.

I started from [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/getting-started.html).

To install Elasticsearch locally, on Mac:
```
curl -L -O https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-7.9.3-darwin-x86_64.tar.gz
``` 
If needed, set your proxy before using `curl`.
```
export http_proxy=http://www-proxy-hqdc.us.oracle.com:80
export https_proxy=http://www-proxy-hqdc.us.oracle.com:80
```
Extract:
```
tar -xvf elasticsearch-7.9.3-darwin-x86_64.tar.gz
```
Start it:
```
cd elasticsearch-7.9.3/bin
./elasticsearch
```
Test it:
```
curl -X GET "localhost:9200/_cat/health?v&pretty"
```
You should see something like
```
epoch      timestamp cluster       status node.total node.data shards pri relo init unassign pending_tasks max_task_wait_time active_shards_percent
1604595248 16:54:08  elasticsearch green           1         1      0   0    0    0        0             0                  -                100.0%
```

We are ready to move on!

---

<style>
body {
  font-family: "Spectral", "Gentium Basic", Cardo , "Linux Libertine o", "Palatino Linotype", Cambria, serif;
  font-size: 100% !important;
  padding-right: 12%;
}
code {
  padding: 0.25em;
	
  white-space: pre;
  font-family: "Tlwg mono", Consolas, "Liberation Mono", Menlo, Courier, monospace;
	
  background-color: #ECFFFA;
  //border: 1px solid #ccc;
  //border-radius: 3px;
}

kbd {
  display: inline-block;
  padding: 3px 5px;
  font-family: "Tlwg mono", Consolas, "Liberation Mono", Menlo, Courier, monospace;
  line-height: 10px;
  color: #555;
  vertical-align: middle;
  background-color: #ECFFFA;
  border: solid 1px #ccc;
  border-bottom-color: #bbb;
  border-radius: 3px;
  box-shadow: inset 0 -1px 0 #bbb;
}

h1,h2,h3,h4,h5 {
  color: #269B7D; 
  font-family: "fira sans", "Latin Modern Sans", Calibri, "Trebuchet MS", sans-serif;
}

</style>

# Kafka CLI

## Opening a bash session interactive terminal with `docker exec -it <container_name> bash`
- From `services.broker.container_name: broker` inside [../docker/docker-compose.yml](../docker/docker-compose.yml), we
  know the `container_name` is `broker`
  - Hence, `docker exec -it broker bash` opens a bash session in such a terminal (for this image bash is available)
    - Now we can use the following commands
      - `[appuser@broker ~]$ printenv PATH` results in 
        `/home/appuser/.local/bin:/home/appuser/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin`
      - After checking a few of these options we find
        `appuser@broker ~]$ ls -la /usr/bin | grep kafka` yields
        ```bash
        -rwxr-xr-x 1 root root     873 Feb 24 12:12 kafka-acls
        -rwxr-xr-x 1 root root     885 Feb 24 12:12 kafka-broker-api-versions
        -rwxr-xr-x 1 root root     881 Feb 24 12:12 kafka-client-metrics
        -rwxr-xr-x 1 root root     872 Feb 24 12:12 kafka-cluster
        -rwxr-xr-x 1 root root     865 Feb 24 12:12 kafka-configs
        -rwxr-xr-x 1 root root     966 Feb 24 12:12 kafka-console-consumer
        -rwxr-xr-x 1 root root     956 Feb 24 12:12 kafka-console-producer
        -rwxr-xr-x 1 root root     970 Feb 24 12:12 kafka-console-share-consumer
        -rwxr-xr-x 1 root root     898 Feb 24 12:12 kafka-consumer-groups
        -rwxr-xr-x 1 root root     960 Feb 24 12:12 kafka-consumer-perf-test
        -rwxr-xr-x 1 root root     883 Feb 24 12:12 kafka-delegation-tokens
        -rwxr-xr-x 1 root root     881 Feb 24 12:12 kafka-delete-records
        -rwxr-xr-x 1 root root     867 Feb 24 12:12 kafka-dump-log
        -rwxr-xr-x 1 root root     878 Feb 24 12:12 kafka-e2e-latency
        -rwxr-xr-x 1 root root     875 Feb 24 12:12 kafka-features
        -rwxr-xr-x 1 root root     877 Feb 24 12:12 kafka-get-offsets
        -rwxr-xr-x 1 root root     874 Feb 24 12:12 kafka-groups
        -rwxr-xr-x 1 root root     868 Feb 24 12:12 kafka-jmx
        -rwxr-xr-x 1 root root     882 Feb 24 12:12 kafka-leader-election
        -rwxr-xr-x 1 root root     875 Feb 24 12:12 kafka-log-dirs
        -rwxr-xr-x 1 root root     882 Feb 24 12:12 kafka-metadata-quorum
        -rwxr-xr-x 1 root root     874 Feb 24 12:12 kafka-metadata-shell
        -rwxr-xr-x 1 root root     815 Feb 24 12:12 kafka-mirror-maker
        -rwxr-xr-x 1 root root     815 Feb 24 12:12 kafka-preferred-replica-election
        -rwxr-xr-x 1 root root     960 Feb 24 12:12 kafka-producer-perf-test
        -rwxr-xr-x 1 root root     895 Feb 24 12:12 kafka-reassign-partitions
        -rwxr-xr-x 1 root root     886 Feb 24 12:12 kafka-replica-verification
        -rwxr-xr-x 1 root root   12374 Feb 24 12:12 kafka-run-class
        -rwxr-xr-x 1 root root    1852 Feb 24 12:12 kafka-server-start
        -rwxr-xr-x 1 root root    3287 Feb 24 12:12 kafka-server-stop
        -rwxr-xr-x 1 root root     965 Feb 24 12:12 kafka-share-consumer-perf-test
        -rwxr-xr-x 1 root root     893 Feb 24 12:12 kafka-share-groups
        -rwxr-xr-x 1 root root     861 Feb 24 12:12 kafka-storage
        -rwxr-xr-x 1 root root     957 Feb 24 12:12 kafka-streams-application-reset
        -rwxr-xr-x 1 root root     890 Feb 24 12:12 kafka-streams-groups
        -rwxr-xr-x 1 root root     875 Feb 24 12:12 kafka-topics
        -rwxr-xr-x 1 root root     880 Feb 24 12:12 kafka-transactions
        -rwxr-xr-x 1 root root     959 Feb 24 12:12 kafka-verifiable-consumer
        -rwxr-xr-x 1 root root     959 Feb 24 12:12 kafka-verifiable-producer
        -rwxr-xr-x 1 root root     964 Feb 24 12:12 kafka-verifiable-share-consumer
        ```
      - `[appuser@broker ~]$ kafka-topics --bootstrap-server localhost:9092 --list` yields
        ```bash
        __consumer_offsets
        _schemas
        user-tracking-avro
        ```
      - `appuser@broker ~]$ curl 'http://schema-registry:8081/subjects'` yields
        `["user-tracking-avro-key","user-tracking-avro-value"][appuser@broker ~]`
      - `[appuser@broker ~]$ curl 'http://schema-registry:8081/subjects/user-tracking-avro-value/versions'` yields `[1]`
      - `[appuser@broker ~]$ curl 'http://schema-registry:8081/subjects/user-tracking-avro-value/versions/1'` yields
        ```bash
        {
          "subject":"user-tracking-avro-value",
          "version":1,
          "id":2,
          "guid":"995630d0-2bb7-0cff-6cb2-2240c738aa92",
          "schemaType":"AVRO",
          "schema": "{\"type\":\"record\",\"name\":\"Product\",\"namespace\":\"com.pluralsight.kafka.model\",\"fields\":[{\"name\":\"Color\",\"type\":{\"type\":\"enum\",\"name\":\"Color\",\"symbols\":[\"GREEN\",\"BLUE\",\"PURPLE\"]}},{\"name\":\"ProductType\",\"type\":{\"type\":\"enum\",\"name\":\"ProductType\",\"symbols\":[\"TSHIRT\",\"DESIGN\"]}},{\"name\":\"DesignType\",\"type\":{\"type\":\"enum\",\"name\":\"DesignType\",\"symbols\":[\"NONE\",\"SUITCASE\",\"CAR\",\"WARNING\"]}}]}",
          "ts":1773589817263,
          "deleted":false
        }
        ```
      - `[appuser@broker ~]$ java -XshowSettings:properties -version` reveals 
        - `java.home = /usr/lib/jvm/java-21-temurin-jre`
        - `java.vm.version = 21.0.10+7-LTS`
  - CTRL+D exits the interactive bash session inside the `broker` container

## Resources
- [https://kafka.apache.org/42/getting-started/](https://kafka.apache.org/42/getting-started/)
- [https://kafka.apache.org/quickstart/](https://kafka.apache.org/quickstart/)
- [https://kafka.apache.org/42/apis/](https://kafka.apache.org/42/apis/)
- [https://cwiki.apache.org/confluence/display/KAFKA/Clients](https://cwiki.apache.org/confluence/display/KAFKA/Clients)
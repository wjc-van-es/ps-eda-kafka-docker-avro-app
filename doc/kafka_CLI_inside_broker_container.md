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

## Calling the kafka CLI directly
Instead of opening a prolonged interactive bash session as explained in the previous section with
`docker exec -it broker bash` we could call the kafka CLI command directly instead of bash. for instance to start a
console producer or consumer:

### starting `kafka-console-producer`
```bash
~/git/ps-eda-kafka-docker-avro-app$ docker exec -it broker kafka-console-producer \
  --bootstrap-server broker:29092 \
  --topic payments
>{"user": "dirty harry", "amount": 5000, "card-nr": "SNARF-1234-XXXX-29092"}
>{"user": "Severus van Buren", "amount": 3300, "card-nr": "SvBu-1313-XXXX-29092"} 
>~/git/ps-eda-kafka-docker-avro-app$  
```
With _Ctrl-D_ the execution is terminated
- Here we should use `--bootstrap-server broker:29092` instead of `--bootstrap-server localhost:9092`, because
  - Although `PLAINTEXT_HOST://0.0.0.0:9092` is configured and `localhost:9092` can be connected initially during 
    bootstrap, 
  - the `kafka-console-producer` cannot reach the `localhost:9092` via loopback, and it is resolved differently for the
    internal (docker network) connection, namely `PLAINTEXT://broker:29092` instead of `PLAINTEXT_HOST://localhost:9092`
    as configured by `KAFKA_ADVERTISED_LISTENERS`
  - So this mismatch between the `--bootstrap-server localhost:9092` command argument and the `PLAINTEXT://broker:29092`
    configuration of `KAFKA_ADVERTISED_LISTENERS` will lead to 
    `Connection to node -1 (localhost/127.0.0.1:9092) could not be established.`
- The difference with `kafka-topics --list` is that it doesn't need to open additional connections to other broker 
  addresses from the metadata for this operation.

### General rule: which `host:port` combination to use when
- From inside containers on the same docker network
  - which is the case when running `docker exec -it <container-name> ...`
  - use as argument for `--bootstrap-server` the `host:port` combination configured for internal use
- From the host, e.g. a Java client running on localhost, use the `host:port` combination configured for the host
  which can be recognized by `<protocol-name>_HOST`
- So from these two environment variables in [../docker/docker-compose.yml](../docker/docker-compose.yml):
  ```yaml
  KAFKA_ADVERTISED_LISTENERS: 'PLAINTEXT://broker:29092,PLAINTEXT_HOST://localhost:9092'
  KAFKA_LISTENERS: 'PLAINTEXT://broker:29092,CONTROLLER://broker:29093,PLAINTEXT_HOST://0.0.0.0:9092'
  ```
  you can derive that with we should use `docker exec -it broker --bootstrap-server broker:29092` since
  - `broker:29092` is the internal (container network) address for the broker
- Therefore, to avoid confusion it is best to use it for both 
  - `kafka-console-producer` (this won't work with `--bootstrap-server localhost:9092`), hence
     ```bash
    docker exec -it broker kafka-console-producer \
      --bootstrap-server broker:29092 \
      --topic payments
     ```
    and
  - `kafka-topic` (although this works fine with `--bootstrap-server localhost:9092` as well), hence
    ```bash
    docker exec -it broker kafka-topics \
      --bootstrap-server broker:29092 --list
    ```
- `localhost:9092` is the external address for the broker that can also be reached from the host of the docker compose
  cluster, provided [../docker/docker-compose.yml](../docker/docker-compose.yml) has published port `9092`, which it
  has as evidenced by the entry `services.broker.ports`:
  ```yaml
  ports:
    - "9092:9092"
    - "9101:9101"
  ```

### Consumer
```bash
~/git/ps-eda-kafka-docker-avro-app$ docker exec -it broker kafka-console-consumer \
  --bootstrap-server broker:29092 \
  --topic payments
{"user": "dirty harry", "amount": 5000, "card-nr": "SNARF-1234-XXXX-29092"}
{"user": "Severus van Buren", "amount": 3300, "card-nr": "SvBu-1313-XXXX-29092"}
^CProcessed a total of 2 messages
~/git/ps-eda-kafka-docker-avro-app$ 
```
Here the execution is terminated with _Ctrl-C_ instead of _Ctrl-D_. Furthermore, the same considerations apply as for
`kafka-console-producer`.


## Resources
- [https://kafka.apache.org/42/getting-started/](https://kafka.apache.org/42/getting-started/)
- [https://kafka.apache.org/quickstart/](https://kafka.apache.org/quickstart/)
- [https://kafka.apache.org/42/apis/](https://kafka.apache.org/42/apis/)
- [https://cwiki.apache.org/confluence/display/KAFKA/Clients](https://cwiki.apache.org/confluence/display/KAFKA/Clients)
- [https://www.perplexity.ai/search/111a5a89-716a-4570-8e4e-8573fde86400](https://www.perplexity.ai/search/111a5a89-716a-4570-8e4e-8573fde86400)
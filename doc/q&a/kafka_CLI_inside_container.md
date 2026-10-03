<img src="https://r2cdn.perplexity.ai/pplx-full-logo-primary-dark%402x.png" style="height:64px;margin-right:32px"/>

# docker exec -it broker kafka-console-producer --bootstrap-server localhost:9292 --topic payments

> [2026-10-03 17:41:08,440] WARN [Producer clientId=console-producer] Connection to node -1 (localhost/127.0.0.1:9292) could not be established. Node may not be available. (org.apache.kafka.clients.NetworkClient)
> [2026-10-03 17:41:08,445] WARN [Producer clientId=console-producer] Bootstrap broker localhost:9292 (id: -1 rack: null isFenced: false) disconnected (org.apache.kafka.clients.NetworkClient)

The error occurs because `localhost:9292` is not reachable **from inside the `broker` container**. Inside that container, `localhost` resolves to the container’s own loopback interface, not your host machine where the Kafka broker is actually listening.[^1_1][^1_2][^1_3]

## Why this happens

- You’re running:

```bash
docker exec -it broker kafka-console-producer \
  --bootstrap-server localhost:9292 \
  --topic payments
```

- The producer runs **inside** the `broker` container.
- Kafka’s bootstrap address `localhost:9292` therefore points to port 9292 **inside** that container.
- But your broker is typically:
    - bound to `0.0.0.0:9292` (or similar) inside the container, and
    - advertised as something like `broker:9292` or `kafka:9292` for other containers, and/or
    - exposed to the host as `host:9292`.\
Inside the container, there’s nothing listening on `127.0.0.1:9292`, so the TCP connection fails and you see:\
`Connection to node -1 (localhost/127.0.0.1:9292) could not be established.`[^1_2][^1_3][^1_4]

Node `-1` specifically means “couldn’t even connect to the bootstrap server”; it’s not a Kafka broker ID yet.[^1_3][^1_2]

## How to fix it

Use the **container name / network alias** (or the advertised listener hostname) instead of `localhost` when running CLI tools from inside the container.

Assuming:

- Your Kafka service in `docker-compose.yml` is named `broker`
- It listens internally on port `9292`
- `KAFKA_ADVERTISED_LISTENERS` is configured for inter-container access as e.g. `PLAINTEXT://broker:9292`

Then run:

```bash
docker exec -it broker kafka-console-producer \
  --bootstrap-server broker:9292 \
  --topic payments
```

If your service is named differently (e.g. `kafka`), use that name:

```bash
docker exec -it broker kafka-console-producer \
  --bootstrap-server kafka:9292 \
  --topic payments
```

This works because `broker` (or `kafka`) resolves via Docker’s internal DNS to the container’s actual IP on the shared network.[^1_5][^1_6][^1_1]

## Verify your listener config

To be sure, inspect your Kafka configuration in the container or compose file:

```bash
docker exec broker env | grep -E 'KAFKA_(ADVERTISED_)?LISTENERS'
```

You want something like:

```text
KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9292
KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://broker:9292
```

or, if you have multiple listeners:

```text
KAFKA_LISTENERS=INTERNAL://0.0.0.0:9292,EXTERNAL://0.0.0.0:9293
KAFKA_ADVERTISED_LISTENERS=INTERNAL://broker:9292,EXTERNAL://localhost:9293
```

Then:

- From **inside** containers on the same Docker network → use `broker:9292` (the `INTERNAL` advertised listener).
- From the **host** → use `localhost:9293` (the `EXTERNAL` advertised listener).[^1_7][^1_8][^1_1]

If you share your `docker-compose.yml` (or the `KAFKA_*` env vars), I can give the exact command tailored to your setup.

<span style="display:none">[^1_10][^1_11][^1_12][^1_13][^1_14][^1_15][^1_9]</span>

<div align="center">⁂</div>

[^1_1]: https://docs.docker.com/guides/kafka/

[^1_2]: https://www.conduktor.io/glossary/kafka-bootstrap-servers

[^1_3]: https://www.ggorantala.dev/kafka-connection-to-node-1-broker-may-not-be-available-2/

[^1_4]: https://www.jianshu.com/p/52a505354bbc

[^1_5]: https://oneuptime.com/blog/post/2026-01-21-kafka-docker-compose/view

[^1_6]: https://www.datacamp.com/tutorial/kafka-docker-explained

[^1_7]: https://www.conduktor.io/kafka/kafka-advertised-host-setting

[^1_8]: https://reintech.io/blog/kafka-docker-guide-containerized-deployments

[^1_9]: https://www.confluent.io/blog/kafka-listeners-explained/

[^1_10]: https://community.cloudera.com/t5/Support-Questions/kafka-console-producer-sh/m-p/91416

[^1_11]: https://hub.docker.com/r/catbee/kafka

[^1_12]: https://hub.docker.com/r/apache/kafka/

[^1_13]: https://oneuptime.com/blog/post/2026-02-02-kafka-docker/view

[^1_14]: https://community.cloudera.com/t5/Support-Questions/kafka-console-producer-sh/td-p/91416

[^1_15]: https://stackoverflow.com/questions/56161345/connection-to-node-1-could-not-be-established-broker-may-not-be-available-or/56161496


---

# same problem when using

docker exec -it broker kafka-console-producer --bootstrap-server broker:9292 --topic payments
settings in docker compose file:
environment:
KAFKA_NODE_ID: 1
KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: 'CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT'
KAFKA_ADVERTISED_LISTENERS: 'PLAINTEXT://broker:29092,PLAINTEXT_HOST://localhost:9092'
KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
KAFKA_JMX_PORT: 9101
KAFKA_JMX_HOSTNAME: localhost
KAFKA_PROCESS_ROLES: 'broker,controller'
KAFKA_CONTROLLER_QUORUM_VOTERS: '1@broker:29093'
KAFKA_LISTENERS: 'PLAINTEXT://broker:29092,CONTROLLER://broker:29093,PLAINTEXT_HOST://0.0.0.0:9092'
KAFKA_INTER_BROKER_LISTENER_NAME: 'PLAINTEXT'
KAFKA_CONTROLLER_LISTENER_NAMES: 'CONTROLLER'
KAFKA_LOG_DIRS: '/tmp/kraft-combined-logs'

# Replace CLUSTER_ID with a unique base64 UUID using "bin/kafka-storage.sh random-uuid"

# See [https://docs.confluent.io/kafka/operations-tools/kafka-tools.html\#kafka-storage-sh](https://docs.confluent.io/kafka/operations-tools/kafka-tools.html#kafka-storage-sh)

CLUSTER_ID: 'MkU3OEVBNTcwNTJENDM2Qk'

The issue is a **port mismatch** between your listeners and the bootstrap server you’re using.[^2_1][^2_2][^2_3]

From your config:

```yaml
KAFKA_ADVERTISED_LISTENERS: 'PLAINTEXT://broker:29092,PLAINTEXT_HOST://localhost:9092'
KAFKA_LISTENERS: 'PLAINTEXT://broker:29092,CONTROLLER://broker:29093,PLAINTEXT_HOST://0.0.0.0:9092'
```

So:

- Internal (container-to-container) listener: `PLAINTEXT://broker:29092`
- External (host) listener: `PLAINTEXT_HOST://localhost:9092`

But you’re trying:

```bash
docker exec -it broker kafka-console-producer \
  --bootstrap-server broker:9292 \
  --topic payments
```

There is **no listener on port 9292** anywhere in your config, only on **29092** and **9092**. That’s why the connection fails.[^2_2][^2_4]

## Correct commands

### From inside the `broker` container (Docker network)

Use the internal listener on **29092**:

```bash
docker exec -it broker kafka-console-producer \
  --bootstrap-server broker:29092 \
  --topic payments
```

This matches `PLAINTEXT://broker:29092` in both `KAFKA_LISTENERS` and `KAFKA_ADVERTISED_LISTENERS`.[^2_1][^2_2]

### From the host machine

Use the external listener on **9092**:

```bash
docker exec -it broker kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic payments
```

But note: this only works if the port is published in `docker-compose.yml`, e.g.:

```yaml
ports:
  - "9092:9092"
```

If you don’t have that `ports` mapping, then from the host you must use the container name via the Docker network (e.g. from another container) or add the port mapping.[^2_5][^2_6]

## Quick checklist

1. Ensure the port you use in `--bootstrap-server` matches one of:
    - `broker:29092` (internal, for containers on the same network)
    - `localhost:9092` (external, for host, if `9092:9092` is published)
2. Don’t use `9292` unless you explicitly configure a listener on that port.

If you run:

```bash
docker exec broker env | grep KAFKA_.*LISTEN
```

you should see exactly the listeners we discussed, confirming there’s no `9292` anywhere.

<span style="display:none">[^2_10][^2_11][^2_12][^2_13][^2_14][^2_15][^2_7][^2_8][^2_9]</span>

<div align="center">⁂</div>

[^2_1]: https://www.tinybird.co/docs/forward/ingest-data/connectors/kafka

[^2_2]: https://stackoverflow.com/questions/64255917/what-does-multiple-kafka-advertised-listeners-mean-when-we-have-only-one-broker

[^2_3]: https://github.com/confluentinc/cp-all-in-one/issues/164

[^2_4]: https://forums.docker.com/t/kafka-client-unable-to-communicate-with-kafka-broker/138644

[^2_5]: https://docs.factorhouse.io/95.2/kpow/provider/confluent-platform

[^2_6]: https://deepwiki.com/conduktor/kafka-stack-docker-compose/3.1-kafka-network-configuration

[^2_7]: https://docs.confluent.io/platform/current/installation/docker/config-reference.html

[^2_8]: https://github.com/Zuehlke/kafka-streaming-technology/blob/main/kafka-security.md

[^2_9]: https://github.com/CallistoLabsNYC/samsa/blob/main/docker-compose-kafka-kraft.yaml

[^2_10]: https://hub.docker.com/r/prodan/kafka-kraft

[^2_11]: https://stackoverflow.com/posts/56023860/revisions

[^2_12]: https://hub.docker.com/r/apache/kafka-native

[^2_13]: https://docs.docker.com/guides/kafka/

[^2_14]: https://www.confluent.io/blog/kafka-listeners-explained/

[^2_15]: https://hub.docker.com/r/apache/kafka/


---

# docker exec -it broker kafka-console-producer --bootstrap-server broker:29292 --topic payments same issue

docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --list
does work, however

The key clue is:

```bash
docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --list 
# works

docker exec -it broker kafka-console-producer --bootstrap-server broker:29292 --topic payments
# fails
```

Two separate issues are happening here.

## 1. Wrong port in your producer command

Your config:

```yaml
KAFKA_LISTENERS: 'PLAINTEXT://broker:29092,CONTROLLER://broker:29093,PLAINTEXT_HOST://0.0.0.0:9092'
KAFKA_ADVERTISED_LISTENERS: 'PLAINTEXT://broker:29092,PLAINTEXT_HOST://localhost:9092'
```

So the valid addresses are:

- Internal (container network): `broker:29092`
- External (host): `localhost:9092` (if port 9092 is published)

But you used:

```bash
--bootstrap-server broker:29292
```

That port (`29292`) does **not** exist in your listeners. It must be `29092`, not `29292`.

Correct command from inside the container:

```bash
docker exec -it broker kafka-console-producer \
  --bootstrap-server broker:29092 \
  --topic payments
```

This should match the `PLAINTEXT://broker:29092` listener.[^3_1][^3_2]

## 2. Why `kafka-topics --bootstrap-server localhost:9092` works from inside

You’re successfully running:

```bash
docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --list
```

From inside the container, `localhost:9092` resolves to the container’s loopback interface. That only works if:

- `PLAINTEXT_HOST` is bound to `0.0.0.0:9092` (it is), and
- The process inside the container can reach that port via loopback (it can).

So `localhost:9092` inside the container hits the `PLAINTEXT_HOST` listener, which is fine for admin tools like `kafka-topics`.

For consistency and to avoid confusion between internal vs external listeners, it’s cleaner to:

- Use `broker:29092` from inside containers on the same Docker network.
- Use `localhost:9092` from the host (or from tools that are meant to use the external listener).


## Recommended commands

From **inside** the `broker` container (your current pattern):

```bash
# List topics (internal listener)
docker exec -it broker kafka-topics \
  --bootstrap-server broker:29092 \
  --list

# Produce messages (internal listener)
docker exec -it broker kafka-console-producer \
  --bootstrap-server broker:29092 \
  --topic payments
```

From the **host** (if you’ve published `9092:9092`):

```bash
docker exec -it broker kafka-topics \
  --bootstrap-server localhost:9092 \
  --list

docker exec -it broker kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic payments
```

If you retry with `broker:29092` (not `29292`) for the producer, the connection error should disappear.

<span style="display:none">[^3_10][^3_11][^3_12][^3_13][^3_14][^3_15][^3_3][^3_4][^3_5][^3_6][^3_7][^3_8][^3_9]</span>

<div align="center">⁂</div>

[^3_1]: https://developer.confluent.io/confluent-tutorials/kafka-on-docker/

[^3_2]: https://deepaksood619.github.io/technologies/kafka/kafka-listeners/

[^3_3]: https://kafka.apache.org/090/getting-started/quickstart/

[^3_4]: https://kafka.apache.org/0100/getting-started/quickstart/

[^3_5]: https://kafka.apache.org/26/getting-started/quickstart/

[^3_6]: https://airbyte.com/data-engineering-resources/kafka-console-producer

[^3_7]: https://github.com/daalvand/kafka-kraft/

[^3_8]: https://dataverses.io/resources/blog/setting-up-kafka-cluster-with-kraft

[^3_9]: https://hub.docker.com/hardened-images/catalog/dhi/kafka/guides

[^3_10]: https://codemia.io/knowledge-hub/path/interact_with_kafka_docker_container_from_outside_of_docker_host

[^3_11]: https://stackoverflow.com/questions/39559597/kafka-docker-cant-produce-or-consume-from-outside-of-docker-container

[^3_12]: https://docs.docker.com/guides/kafka/

[^3_13]: https://oneuptime.com/blog/post/2026-02-02-kafka-docker/view

[^3_14]: https://hub.docker.com/r/catbee/kafka

[^3_15]: https://www.jianshu.com/p/52a505354bbc


---

# OK I checked and

docker exec -it broker kafka-console-producer --bootstrap-server broker:29092 --topic payments
works for me, but
docker exec -it broker kafka-console-producer --bootstrap-server localhost:9092 --topic payments
doesn't
unlike
docker exec -it broker kafka-topics --bootstrap-server localhost:9092 --list
that works fine.
So this is due to kafka-topics being able to reach port 9092 via loopback, whilst kafka-console-producer does not?

Not exactly — it’s not that `kafka-console-producer` can’t reach port 9092 via loopback. The difference is in **how the two tools use the bootstrap server and advertised listeners after the initial connection**.[^4_1]

## What’s happening under the hood

Both `kafka-topics --list` and `kafka-console-producer`:

1. Open an initial TCP connection to the bootstrap server you specify (`localhost:9092` in your case).
2. Ask the broker for **cluster metadata** (topic list, broker endpoints, etc.).
3. The broker replies with metadata that includes its **advertised listeners**, e.g.:

```text
PLAINTEXT://broker:29092
PLAINTEXT_HOST://localhost:9092
```


The key difference is what they do next.

### `kafka-topics --list`

- For a simple `--list`, the client:
    - Connects to `localhost:9092`
    - Fetches metadata
    - Prints the topic list
- It doesn’t need to open **additional** connections to other broker addresses from the metadata for this operation.
- So as long as the initial TCP handshake to `localhost:9092` works (which it does inside the container, via the `PLAINTEXT_HOST` listener bound to `0.0.0.0:9092`), the command succeeds.[^4_2]


### `kafka-console-producer`

- The producer:
    - Connects to `localhost:9092`
    - Fetches metadata for the target topic (`payments`)
    - The metadata tells it which broker(s) and **which advertised address** to use for producing
- The producer then tries to connect to the **advertised broker endpoint** from the metadata, not necessarily the same `localhost:9092` you passed in.
- In a Docker setup with multiple listeners, the broker may advertise something like `broker:29092` as the primary endpoint for internal clients.
- Inside the container, if the producer ends up trying to reach an advertised host/port combination that isn’t reachable via loopback (or resolves differently), you get:

```text
Connection to node -1 (localhost/127.0.0.1:9092) could not be established.
```

even though the initial bootstrap connection succeeded.[^4_3][^4_1]

In other words: `kafka-topics --list` is “metadata-only and done”, while `kafka-console-producer` must maintain an active data connection to the broker endpoint returned in metadata, and that endpoint is influenced by `advertised.listeners`.

## Why `broker:29092` works cleanly

When you use:

```bash
docker exec -it broker kafka-console-producer \
  --bootstrap-server broker:29092 \
  --topic payments
```

- The bootstrap address matches the internal listener: `PLAINTEXT://broker:29092`.
- The metadata the broker returns also advertises `broker:29092` for that listener.
- The producer then connects to `broker:29092`, which resolves correctly inside the container via Docker DNS.
- No mismatch between “bootstrap” and “advertised” endpoints → no connection error.[^4_1]

When you use:

```bash
docker exec -it broker kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic payments
```

- Bootstrap succeeds (TCP to `localhost:9092` works).
- Metadata comes back with advertised listeners that include `broker:29092` and/or `localhost:9092` depending on listener name and client binding.
- The producer’s internal logic for choosing which advertised endpoint to use can differ from `kafka-topics`, and in your setup it ends up trying an address/port combination that fails from inside the container.

This is a classic Docker + `advertised.listeners` symptom: admin/list commands may “work” with a suboptimal listener, but producers/consumers expose the misalignment.[^4_3][^4_1]

## Practical rule of thumb

For your current config:

```yaml
KAFKA_LISTENERS: 'PLAINTEXT://broker:29092,CONTROLLER://broker:29093,PLAINTEXT_HOST://0.0.0.0:9092'
KAFKA_ADVERTISED_LISTENERS: 'PLAINTEXT://broker:29092,PLAINTEXT_HOST://localhost:9092'
```

Use:

- **From inside containers on the same Docker network**:

```bash
--bootstrap-server broker:29092
```

for both `kafka-topics`, `kafka-console-producer`, and `kafka-console-consumer`.
- **From the host** (if `9092:9092` is published):

```bash
--bootstrap-server localhost:9092
```


Stick to `broker:29092` for CLI tools run via `docker exec` and you’ll avoid these subtle listener/metadata mismatches.

<span style="display:none">[^4_10][^4_11][^4_12][^4_13][^4_14][^4_15][^4_4][^4_5][^4_6][^4_7][^4_8][^4_9]</span>

<div align="center">⁂</div>

[^4_1]: https://www.ggorantala.dev/kafka-connection-to-node-1-broker-may-not-be-available/

[^4_2]: https://www.conduktor.io/kafka/kafka-topics-cli-tutorial

[^4_3]: https://stackoverflow.com/questions/60496310/console-producer-error-connecting-to-node-kafka19092-org-apache-kafka-clients

[^4_4]: https://kafka.apache.org/0101/getting-started/quickstart/

[^4_5]: https://kafka.apache.org/22/getting-started/quickstart/

[^4_6]: https://camel.apache.org/camel-kafka-connector/4.14.x/user-guide/getting-started/try-it-out-locally.html

[^4_7]: https://kafka.apache.org/quickstart/

[^4_8]: https://kafka.apache.org/10/getting-started/quickstart/

[^4_9]: https://hevodata.com/learn/kafka-console-producer/

[^4_10]: https://www.conduktor.io/kafka/kafka-producer-cli-tutorial

[^4_11]: https://hub.docker.com/r/apache/kafka

[^4_12]: https://kafka.apache.org/0100/getting-started/quickstart/

[^4_13]: https://kafka.apache.org/26/getting-started/quickstart/

[^4_14]: https://developer.confluent.io/confluent-tutorials/kafka-on-docker/

[^4_15]: https://docs.docker.com/guides/kafka/


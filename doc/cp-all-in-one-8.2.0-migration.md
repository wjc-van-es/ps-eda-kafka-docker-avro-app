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

# Migration of cp-all-in-one-comminity from 7.2.1 to 8.2.0

## Context
The cp-all-in-one docker compose is a docker compose deployment script with all the 
Confluent components that are available under the community license, with their corresponding version 8.0.0 tagged 
images.

The most notable difference is that zookeeper is no longer necessary in favor of KRaft and that therefore the 
configuration of the broker is more elaborate with more environment variables to set.
- [https://github.com/confluentinc/cp-all-in-one/tree/8.0.0-post](https://github.com/confluentinc/cp-all-in-one/tree/8.0.0-post)
- [https://github.com/confluentinc/cp-all-in-one/blob/8.0.0-post/cp-all-in-one-community/docker-compose.yml](https://github.com/confluentinc/cp-all-in-one/blob/8.0.0-post/cp-all-in-one-community/docker-compose.yml)
- [https://docs.confluent.io/platform/current/installation/versions-interoperability.html#cp-and-apache-ak-compatibility](https://docs.confluent.io/platform/current/installation/versions-interoperability.html#cp-and-apache-ak-compatibility)
- [https://github.com/confluentinc/cp-demo](https://github.com/confluentinc/cp-demo)
- [https://docs.confluent.io/kafka-client/overview.html#java-client-support](https://docs.confluent.io/kafka-client/overview.html#java-client-support)

### Compatibility
- From [https://docs.confluent.io/platform/current/installation/versions-interoperability.html#cp-and-apache-ak-compatibility](https://docs.confluent.io/platform/current/installation/versions-interoperability.html#cp-and-apache-ak-compatibility)
  we learn that
  - The `confluentinc/cp-kafka:8.0.0` is compatible with `org.apache.kafka:kafka-clients:4.0.x`
  - The `confluentinc/cp-kafka:8.2.x` is compatible with `org.apache.kafka:kafka-clients:4.2.x`
- From [https://docs.confluent.io/platform/current/installation/docker/image-reference.html](https://docs.confluent.io/platform/current/installation/docker/image-reference.html)
  we learn that we can look on DockerHub for the latest available stable image per component described in
  [https://github.com/confluentinc/cp-all-in-one/blob/8.0.0-post/cp-all-in-one-community/docker-compose.yml](https://github.com/confluentinc/cp-all-in-one/blob/8.0.0-post/cp-all-in-one-community/docker-compose.yml)
- We can take this compose file and only declare the containers we need and update the image to the latest available tag
  from [https://hub.docker.com/u/confluentinc/](https://hub.docker.com/u/confluentinc/)
- For this project we would only need 2 components:
  - [https://hub.docker.com/r/confluentinc/cp-kafka/tags?name=8.2.0](https://hub.docker.com/r/confluentinc/cp-kafka/tags?name=8.2.0)
  - As zookeeper is replaced by KRaft we need to take this into account in our configurations:  
    [https://docs.confluent.io/platform/current/installation/docker/config-reference.html#required-ak-configurations-for-kraft-mode](https://docs.confluent.io/platform/current/installation/docker/config-reference.html#required-ak-configurations-for-kraft-mode)
    - [https://docs.confluent.io/platform/current/kafka-metadata/config-kraft.html#inter-broker-listeners](https://docs.confluent.io/platform/current/kafka-metadata/config-kraft.html#inter-broker-listeners)
    - [https://docs.confluent.io/platform/current/installation/migrate-zk-kraft.html#phases-of-migration](https://docs.confluent.io/platform/current/installation/migrate-zk-kraft.html#phases-of-migration)
  - [https://hub.docker.com/r/confluentinc/cp-schema-registry/tags?name=8.2.0](https://hub.docker.com/r/confluentinc/cp-schema-registry/tags?name=8.2.0)
- Then we can search for the latest available compatible kafka client libraries

## All necessary modifications
- For [`../docker/docker-compose.yml`](../docker/docker-compose.yml) copy the first two services from
  [resources/cp-all-in-one-ce-8.0.0/docker-compose.yml](resources/cp-all-in-one-ce-8.0.0/docker-compose.yml)
  - the configuration stays unaltered, but the image tags are
    - `services.broker.image: confluentinc/cp-kafka:8.2.0` and
    - `services.schema-registry.image: confluentinc/cp-schema-registry:8.2.0`
- The corresponding client libraries declared in `/project/dependencyManagement/dependencies` of 
  [../pom.xml](../pom.xml) are
  - `org.apache.kafka:kafka-clients:4.2.0`
  - `io.confluent:kafka-avro-serializer:8.1.1` (as `8.2.0` is not available yet)
  - `org.slf4j:slf4j-reload4j:1.7.36` this version is derived from the transitive depenency of
    `org.slf4j:slf4j-api:1.7.36` under `org.apache.kafka:kafka-clients:4.2.0`
    - see [https://mvnrepository.com/artifact/org.slf4j/slf4j-parent/1.7.36](https://mvnrepository.com/artifact/org.slf4j/slf4j-parent/1.7.36)
    - and [file:///home/willem/.m2/repository/org/slf4j/slf4j-parent/1.7.36/slf4j-parent-1.7.36.pom](../../../.m2/repository/org/slf4j/slf4j-parent/1.7.36/slf4j-parent-1.7.36.pom)
- All other dependencies just use the latest available
- For all plugins, use the latest available as well

## Test execution
- Build the project with `mvn clean package -e`, 
- The [../user-tracking-interface](../user-tracking-interface) module, which uses the `org.apache.avro:avro` plugin to 
  generate Java class source code from schema definitions at
  - [../user-tracking-interface/src/main/resources/avro/user_schema.avsc](../user-tracking-interface/src/main/resources/avro/user_schema.avsc) and
  - [../user-tracking-interface/src/main/resources/avro/product_schema.avsc](../user-tracking-interface/src/main/resources/avro/product_schema.avsc)
- To be able to import the classes from this module, which we need to mark the
  [../user-tracking-interface/src/main/generated](../user-tracking-interface/src/main/generated) directory as a 
  _Generated Sources Root_ in IntelliJ. Only after this, the following import statements in both the consumer & producer
  can be recognized inside IntelliJ:
  - `import com.pluralsight.kafka.model.Product;` and
  - `import com.pluralsight.kafka.model.User;`
- After this the example still works in accordance of the description at [../README.md](../README.md)
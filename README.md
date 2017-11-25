# Vertx-DistributedWebChat
Distributed web chat with Java and Vertx library

# Prerequisites

* Java 8
* Maven 3.3.9
* Docker 17.09.0-ce

## SetUp

```sh
$ mvn install
```

## How to start instances

This application can works with multiples node, which run with docker

Run first node
```sh
./fullBuildAndRun.sh 5000
```
Run second node
```sh
./fullBuildAndRun.sh 8080
```

Open in your browser localhost:5000 and localhost:8080, then you can chat between nodes.

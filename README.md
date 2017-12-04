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

## How to start a single instance

```sh
./run.sh
```

## How to start instances

This application can works with multiples nodes, which run with docker

Run first node
```sh
./fullBuildAndRun.sh 9000
```
Run second node
```sh
./fullBuildAndRun.sh 9001
```

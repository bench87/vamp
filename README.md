<img src="http://vamp.io/images/logo.svg" width="250px" />

[Website](http://vamp.io) |
[Documentation](http://vamp.io/documentation/how-vamp-works/architecture-and-components/) |
[Installation Guide](http://vamp.io/documentation/installation/) |

[![Build Status](https://travis-ci.org/magneticio/vamp.svg?branch=master)](https://travis-ci.org/magneticio/vamp-core) [ ![Download](https://api.bintray.com/packages/magnetic-io/downloads/vamp/images/download.svg) ](https://bintray.com/magnetic-io/downloads/vamp/_latestVersion) [![Join the chat at https://gitter.im/magneticio/vamp](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/magneticio/vamp?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Vamp is the Very Awesome Microservices Platform. Vamp's core features are a platform-agnostic microservices DSL, easy A-B testing/canary releasing on everything and a deep and extendable metrics engine that monitors everything and directly feeds back into your services.

Vamp is open source and mostly written in Scala, with some
parts in Go.

## Deploy Vamp on your laptop

Vamp can run on your laptop with one command. Check out our [Hello World quick start](http://vamp.io/documentation/installation/hello-world/). This should be enough to kick the tires.

## Using Vamp and more

For documentation on using Vamp and all other info please check [vamp.io](http://vamp.io/documentation/using-vamp/artifacts/) and
take some time to walk through the [getting started](http://vamp.io/documentation/tutorials/).

## Contributing

Vamp is open source. Any contributions are welcome. Please check [our contribution guidelines](https://github.com/magneticio/vamp/blob/master/CONTRIBUTING.md)


## 개발환경 셋팅
최소한의 개발환경을 갖추기 위해서 간단히 필요한 설정은 아래와 같습니다. key-value-store는 redis를 쓴다는 가정으로 작성되었고 redis를 로컬에 실행하는 방법은 아래에 있습니다.

#### configuration
vamp/persistence/src/main/resources/reference.conf
```
vamp {
  namespace = "vamp"
  bootstrap.timeout = 3 seconds

  container-driver {
    type = "marathon"
    network = "bridge"
    label-namespace = "io.vamp"
    response-timeout = 30 seconds # timeout for container operations
  }

  container-driver {
    # type = "" # marathon
    mesos.url = "${MESOS}:5050"
    marathon {
      user = ""
      password = ""
      token = ""
      url = "http://${MARATHON}:8080"
      sse = true
      namespace-constraint = []
      cache {
        read-time-to-live = 30 seconds  # get requests can't be sent to Marathon more often than this, unless cache entries are invalidated
        write-time-to-live = 30 seconds # post/put/delete requests can't be sent to Marathon more often than this, unless cache entries are invalidated
        failure-time-to-live = 30 seconds # ttl in case of a failure (read/write)
      }
    }
  }
  gateway-driver {
    host = "" # vamg gateway agent host
    response-timeout = 30 seconds # timeout for gateway operations
    marshallers = [
      {
        type = "haproxy"
        name = "1.7"
        template {
          file = "" # if specified it will override the resource template
          resource = "/io/vamp/gateway_driver/haproxy/template.twig" # it can be empty
        }
      }
    ]
  }

  persistence {
    response-timeout = 5 seconds #
    database {
      type: "in-memory" # in-memory, file, postgres, mysql, sqlserver
      sql {
        url = ""
        user = ""
        password = ""
        delay = 3s
        table = "Artifacts"
        synchronization.period = 0s
      }
      file {
        directory = ""
      }
    }
    key-value-store {
      type = "redis"
      base-path = "/vamp/${namespace}"
      cache.read-ttl = 5m
    }
  }

  pulse {
    type = "no-store" # no-store
    response-timeout = 30 seconds # timeout for pulse operations
  }

  workflow-driver {
    type = "marathon" # it's possible to combine (csv): 'type_x,type_y'
    response-timeout = 30 seconds # timeout for container operations
    workflow {
      deployables = []
      scale {         # default scale, if not specified in workflow
        instances = 1
        cpu = 0.1
        memory = 64MB
      }
    }
  }

  gateway-driver {
    host = "" # vamg gateway agent host
    response-timeout = 30 seconds # timeout for gateway operations
    marshallers = [
      {
        type = "haproxy"
        name = "1.7"
        template {
          file = "" # if specified it will override the resource template
          resource = "/io/vamp/gateway_driver/haproxy/template.twig" # it can be empty
        }
      }
    ]
  }
}

akka {

  loglevel = "INFO"
  log-dead-letters = 0
  log-config-on-start = off
  log-dead-letters-during-shutdown = off
  loggers = ["akka.event.slf4j.Slf4jLogger"]
  event-handlers = ["akka.event.slf4j.Slf4jEventHandler"]

  actor.default-mailbox.mailbox-type = "akka.dispatch.SingleConsumerOnlyUnboundedMailbox"

  default-dispatcher.fork-join-executor.pool-size-max = 32
  jvm-exit-on-fatal-error = true

  http.server.server-header = ""
}

```

#### Run Redis on docker
key-value-store로 redis를 설정 했으므로 redis를 로컬에 도커로 간단히 띄워줍니다.
```
docker run -d --name vamp-redis -p 6379:6379 redis:latest
```

### persistence mysql을 사용하고 싶은 경우
[vamp-lifter](https://github.com/magneticio/vamp-lifter)를 사용하거나 그냥 아래 쿼리를 데이터베이스에 먼저 실행해서 데이터베이스와 테이블이 없어서 엑세스 못하는 상황을 방지해야 합니다.
```
docker run -d --name vamp-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -d mysql:5.7

CREATE DATABASE vamp;
CREATE TABLE IF NOT EXISTS `Artifacts` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `Record` MEDIUMTEXT,
  PRIMARY KEY (`ID`)
) DEFAULT CHARSET=utf8;
```

### Run Vamp
```
sbt run
```

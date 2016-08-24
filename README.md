* yet another short URL redirect service
* Play Framework 2.4
* Backend depends on ElasticSearch (HTTP API)

* Docker image build process might look something like this
```
sbt docker:publishLocal
tag=$(docker images | egrep "^play-shorty-service" | awk '{print $3}')
docker tag $tag plinde/play-shorty-service
docker images | grep play
docker push plinde/play-shorty-service
```

* To run this service in a Docker container, use this example. ElasticSearch parameters are required at this time.
* We use dockerEntrypoint overloading trick described in below article: http://blog.michaelhamrah.com/2014/11/clustering-akka-applications-with-docker-version-3/
```
docker run -p 9000:9000 plinde/play-shorty-service -DELASTICSEARCH_PROTO="http" -DELASTICSEARCH_HOST="192.168.1.71" -DELASTICSEARCH_PORT="9200"  -DELASTICSEARCH_USER="elastic" -DELASTICSEARCH_PASS="changeme"
```

* https://hub.docker.com/r/plinde/play-shorty-service/

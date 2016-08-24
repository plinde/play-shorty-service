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
```
docker run -p 9000:9000  -e "ELASTICSEARCH_PROTO=http" -e "ELASTICSEARCH_HOST=192.168.1.71" -e "DELASTICSEARCH_PORT=9200"  -e "ELASTICSEARCH_USER=elastic" -e "DELASTICSEARCH_PASS=changeme" plinde/play-shorty-service
```

* https://hub.docker.com/r/plinde/play-shorty-service/

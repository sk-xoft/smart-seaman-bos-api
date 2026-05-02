# smart-seaman-bos-api
smart-seaman-bos-api_1


## Command docker
docker build -t xoftspace/smart-seaman-bos-api:0.1 .
docker build -t xoftspace/smart-seaman-bos-api:0.2 .
docker build -t xoftspace/smart-seaman-bos-api:0.3 .
docker build -t xoftspace/smart-seaman-bos-api:0.4 .
docker build -t xoftspace/smart-seaman-bos-api:0.5 .
docker build -t xoftspace/smart-seaman-bos-api:0.6 .
docker build -t xoftspace/smart-seaman-bos-api:0.7 .

docker run --name smart-seaman-bos-api -d \
-e COMPANY='smart-seaman' \
-e ENV='dev' \
-it -p 20000:8080/tcp \
xoftspace/smart-seaman-bos-api:0.1


docker run --name smart-seaman-bos-api-0.7 -d \
-e COMPANY='smart-seaman' \
-e ENV='dev' \
-it -p 20000:8080/tcp \
-v /home/ssmuser/apps-logs-service/smart-seaman-bos-api/logs:/apps-logs-service/smart-seaman-bos-api/logs \
xoftspace/smart-seaman-bos-api:0.7

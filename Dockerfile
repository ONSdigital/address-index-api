FROM openjdk:8u212-b04-jre-slim-stretch
WORKDIR /
ADD . .
ENV ONS_AI_LIBRARY_PATH ..
ENV ONS_AI_RESOURCE_PATH /../
ENV ONS_AI_PARSER_TOKENS_FOLDER /../
CMD address-index-server-1.0.0/bin/address-index-server

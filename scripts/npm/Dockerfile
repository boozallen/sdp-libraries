FROM node:slim
COPY container-entrypoint.sh container-entrypoint.sh
COPY package*.json ./
RUN apt-get update && \
    apt-get install -y git && \
    mkdir -p /home/antora && \
    useradd antora -b /home/antora && \
    chmod 777 /home/antora && \
    chmod +x container-entrypoint.sh && \
    npm install 
USER antora
WORKDIR /docs 
ENTRYPOINT ["/container-entrypoint.sh"]

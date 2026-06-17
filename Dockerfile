FROM eclipse-temurin:21-jdk-jammy

# Install system packages + Python + pip + nginx + ssh
RUN apt-get update && apt-get install -y \
    nginx \
    openssh-server \
    git \
    maven \
    python3 \
    python3-pip \
    && pip3 install ansible \
    && rm -rf /var/lib/apt/lists/*

# Configure SSH on port 2222
RUN mkdir /var/run/sshd && \
    echo 'root:Hello@123' | chpasswd && \
    sed -i 's/#Port 22/Port 2222/' /etc/ssh/sshd_config && \
    sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config

# NGINX config
COPY nginx.conf /etc/nginx/sites-available/default

# Write entrypoint script
RUN printf '#!/bin/bash\n\
service ssh start\n\
service nginx start\n\
cd /app\n\
mvn clean package -DskipTests\n\
java -jar target/*.jar\n' > /entrypoint.sh \
    && chmod +x /entrypoint.sh

WORKDIR /app
COPY . /app

EXPOSE 8085 2222

ENTRYPOINT ["/entrypoint.sh"]
FROM jboss/wildfly:21.0.0.Final

USER root

# CentOS 7 is EOL, use vault repositories
RUN sed -i 's/mirrorlist/#mirrorlist/g' /etc/yum.repos.d/CentOS-* && \
    sed -i 's|#baseurl=http://mirror.centos.org|baseurl=http://vault.centos.org|g' /etc/yum.repos.d/CentOS-* && \
    yum install -y nc && \
    yum clean all

USER jboss

COPY ./wait-for-it.sh /opt/jboss/wait-for-it.sh
COPY ./infrastructure/kafka-rar-0.6.0.rar /opt/jboss/wildfly/standalone/deployments/kafka-rar.rar
COPY ./infrastructure/standalone-full.xml /opt/jboss/wildfly/standalone/configuration/standalone-full.xml
COPY ./target/battleship-service.war /opt/jboss/wildfly/standalone/deployments/

CMD ["/opt/jboss/wait-for-it.sh", "kafka:29092", "-t", "60", "--", "/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone-full.xml", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0", "--debug", "*:8787"]

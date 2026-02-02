FROM jboss/wildfly:21.0.0.Final
COPY ./infrastructure/kafka-rar-0.6.0.rar /opt/jboss/wildfly/standalone/deployments/kafka-rar.rar
COPY ./infrastructure/standalone-full.xml /opt/jboss/wildfly/standalone/configuration/standalone-full.xml
COPY ./target/battleship-computer-service.war /opt/jboss/wildfly/standalone/deployments/
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone-full.xml", "-b", "0.0.0.0","-bmanagement","0.0.0.0", "--debug", "*:8787"]
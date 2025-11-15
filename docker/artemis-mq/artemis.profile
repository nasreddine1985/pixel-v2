# Artemis Profile Configuration
# PIXEL-V2 specific settings

# JVM Arguments
ARTEMIS_CLUSTER_PROPS="-Dactivemq.artemis.client.global.thread.pool.max.size=30 -Dhawtio.realm=activemq -Dhawtio.offline=true -Dhawtio.role=admin -Djolokia.policyLocation=file:/var/lib/artemis-instance/etc/jolokia-access.xml"

# Logging configuration
JAVA_ARGS="$JAVA_ARGS -XX:+PrintClassHistogram -XX:+UseG1GC -XX:+UseStringDeduplication"

# Memory settings for PIXEL-V2 workload
JAVA_ARGS="$JAVA_ARGS -Xms512M -Xmx2G"

# Enable JMX for monitoring
JAVA_ARGS="$JAVA_ARGS -Dcom.sun.management.jmxremote=true"
JAVA_ARGS="$JAVA_ARGS -Dcom.sun.management.jmxremote.port=3000"
JAVA_ARGS="$JAVA_ARGS -Dcom.sun.management.jmxremote.rmi.port=3001"
JAVA_ARGS="$JAVA_ARGS -Dcom.sun.management.jmxremote.ssl=false"
JAVA_ARGS="$JAVA_ARGS -Dcom.sun.management.jmxremote.authenticate=false"

# PIXEL-V2 specific properties
JAVA_ARGS="$JAVA_ARGS -Dpixel.artemis.mode=production"
JAVA_ARGS="$JAVA_ARGS -Dpixel.message.persistence=true"
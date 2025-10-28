import javax.jms.*;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import java.io.*;

public class LoadTestSender {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Usage: java LoadTestSender <brokerUrl> <username> <password> <queueName>");
            System.exit(1);
        }
        
        String brokerUrl = args[0];
        String username = args[1];
        String password = args[2];
        String queueName = args[3];
        
        try {
            // Create connection factory
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            factory.setUser(username);
            factory.setPassword(password);
            
            // Create connection and session
            try (Connection connection = factory.createConnection();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                
                connection.start();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Queue queue = session.createQueue(queueName);
                MessageProducer producer = session.createProducer(queue);
                
                String line;
                int count = 0;
                StringBuilder messageBuilder = new StringBuilder();
                
                // Read message content from stdin
                while ((line = reader.readLine()) != null) {
                    if (line.equals("---MESSAGE_END---")) {
                        if (messageBuilder.length() > 0) {
                            // Create and send message
                            TextMessage message = session.createTextMessage(messageBuilder.toString());
                            message.setStringProperty("MessageType", "pacs.008.001.08");
                            message.setStringProperty("Source", "load-test");
                            message.setLongProperty("InjectTimestamp", System.currentTimeMillis());
                            
                            producer.send(message);
                            count++;
                            
                            if (count % 1000 == 0) {
                                System.err.println("Sent " + count + " messages...");
                            }
                            
                            messageBuilder.setLength(0);
                        }
                    } else {
                        messageBuilder.append(line).append("\n");
                    }
                }
                
                System.out.println("Total messages sent: " + count);
                
                producer.close();
                session.close();
            }
        } catch (Exception e) {
            System.err.println("Error sending messages: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

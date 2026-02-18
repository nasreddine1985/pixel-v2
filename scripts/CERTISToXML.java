package com.bnpp.bw;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * CERTIS to XML converter - Java 17 version
 * Converts CERTIS message format to XML with improved error handling
 * 
 * @author 822042
 */
public class CERTISToXML implements CERTISConstants {

	/** Private Global variables **/
	private TransformerHandler th;
	private StreamResult out;
	private AttributesImpl atts;
	private int lineNumber = 0;
	private boolean eof = false;
	private String rejectMsg = "";
	private String responseCode = "SUCCESS";
	private String responseDescription = "Successfully converted the message.";
	
	private static final Map<String, String> props = new HashMap<>();
	private static final org.apache.logging.log4j.Logger loggerBW = 
			org.apache.logging.log4j.LogManager.getLogger("bw.logger");

	static {
		Properties properties = new Properties();
		String strPropsPath = "certis.properties";
		
		try (InputStream objInputStream = CERTISToXML.class.getResourceAsStream(strPropsPath)) {
			if (objInputStream == null) {
				loggerBW.error("Properties file not found: {}", strPropsPath);
			} else {
				properties.load(objInputStream);
				props.putAll((Map) properties);
			}
		} catch (IOException e) {
			loggerBW.error("Exception while reading properties file: {}", strPropsPath, e);
		} catch (Exception e) {
			loggerBW.error("Unexpected exception while reading properties: {}", strPropsPath, e);
		}
	}
	
	private String getProperty(String key) {
		return props.get(key);
	}

	/**
	 * Gets pattern value from CERTIS constants
	 * 
	 * @param patternName the pattern name
	 * @return the pattern value
	 * @throws IllegalArgumentException if pattern not found
	 * @throws SecurityException if access denied
	 * @throws IllegalAccessException if field not accessible
	 * @throws NoSuchFieldException if field doesn't exist
	 */
	private String getPatternValue(String patternName) 
			throws IllegalArgumentException, SecurityException, 
			       IllegalAccessException, NoSuchFieldException {
		return (String) CERTISConstants.class.getDeclaredField(patternName).get(null);
	}

	/**
	 * Parses CERTIS file and converts to XML
	 * 
	 * @param fileName the file to parse
	 * @param msgNbr number of messages to parse
	 * @param position starting position in file
	 * @return array with [0]=XML result, [1]=reject messages
	 */
	@SuppressWarnings("finally")
	public String[] parseCertisFile(String fileName, int msgNbr, int position) {
		String[] returnValue = new String[2];
		
		try (BufferedReader in = new BufferedReader(
				new InputStreamReader(
						new FileInputStream(fileName),
						Charset.forName(getProperty(READER_ENCODING))))) {

			Writer writer = new StringWriter();
			out = new StreamResult(writer);
			lineNumber = position;
			
			String msgCertis = "";
			String str = null;
			int index = 0;
			boolean certisMsgStart;
			
			// Skip the first 'position' lines
			if (position == 0) {
				str = in.readLine();
			} else {
				for (int i = 0; i <= position; i++) {
					str = in.readLine();
				}
			}
			
			// Initialize XML
			initXML();

			// Read the first 'msgNbr' CERTIS messages
			while (index < msgNbr && !eof) {
				certisMsgStart = true;	
				while (str != null && certisMsgStart) {
					msgCertis += msgCertis.isEmpty() ? str : DELIM + str;
					str = in.readLine();
					if (str != null && str.startsWith(CERTIS_Msg_Starting)) {
						certisMsgStart = false;
					}
					lineNumber++;
				}
				
				// Build CERTIS message
				if (!msgCertis.isEmpty()) {
					processCertisMessage(msgCertis);
				}
				msgCertis = "";
				index++;
				
				if (str == null) {
					eof = true;
				}
			}
			
			// Close XML result
			closeXML();
			returnValue[0] = writer.toString();
			returnValue[1] = rejectMsg;
			
		} catch (FileNotFoundException e) {
			responseCode = "FAILURE";
			responseDescription = "File: " + fileName + " doesn't exist";
			loggerBW.error(responseDescription, e);
		} catch (Exception e) {
			responseCode = "FAILURE";
			responseDescription = e.getMessage();
			loggerBW.error("Error processing CERTIS file", e);
		} finally {
			return returnValue;
		}
	}

	/**
	 * Process a single CERTIS message with improved error handling
	 * 
	 * @param message the CERTIS message
	 */
	private void processCertisMessage(String message) {
		try {
			// Extract message type with validation
			if (message.length() < 5) {
				loggerBW.warn("Message too short to extract type: {}", message);
				return;
			}
			
			String msgTypeStr = message.substring(3, 5).trim();
			
			// FIX: Handle the "For input string \":0\"" error
			// Remove any non-numeric characters before parsing
			msgTypeStr = msgTypeStr.replaceAll("[^0-9]", "");
			
			if (msgTypeStr.isEmpty()) {
				loggerBW.warn("Invalid message type in message: {}", message.substring(0, Math.min(20, message.length())));
				return;
			}
			
			int msgType = Integer.parseInt(msgTypeStr);
			doit(message, msgType);
			
		} catch (NumberFormatException e) {
			responseCode = "FAILURE";
			responseDescription = "Invalid message type format: " + e.getMessage();
			loggerBW.error("Cannot parse message type from message: {}", 
					message.substring(0, Math.min(50, message.length())), e);
		} catch (Exception e) {
			responseCode = "FAILURE";
			responseDescription = "Error processing message: " + e.getMessage();
			loggerBW.error("Error in processCertisMessage", e);
		}
	}

	/**
	 * Builds XML for one CERTIS message
	 * 
	 * @param message the message content
	 * @param msgType the message type
	 */
	private void doit(String message, int msgType) {
		String previousElt = null;

		try {
			String nodeName0 = determineNodeName(msgType);
			
			if (nodeName0 != null) {
				atts.clear();
				th.startElement("", "MESSAGECERTIS", "MESSAGECERTIS", atts);
				th.startElement("", nodeName0, nodeName0, atts);
				
				StringTokenizer st = new StringTokenizer(message, DELIM);
				
				if ("Reject".equals(nodeName0)) {
					rejectMsg = rejectMsg + "\n" + message;
				}
				
				previousElt = "";
				while (st.hasMoreTokens()) {
					String str = st.nextToken();
					
					if (!str.startsWith("   ") && !previousElt.isEmpty()) {
						process(previousElt);
						previousElt = str;
					} else {
						previousElt += previousElt.isEmpty() ? str : DELIM + str;
					}
				}
				
				process(previousElt);
				th.endElement("", nodeName0, nodeName0);
				th.endElement("", "MESSAGECERTIS", "MESSAGECERTIS");
			}
			
		} catch (Exception e) {
			responseCode = "FAILURE";
			responseDescription = e.getMessage();
			loggerBW.error("Error in doit method", e);
		}
	}

	/**
	 * Determines the XML node name based on message type
	 * 
	 * @param msgType the message type
	 * @return the node name or null if type not recognized
	 */
	private String determineNodeName(int msgType) {
		return switch (msgType) {
			case 1, 11, 12, 13, 14, 96, 97,  // CP
				 15, 16, 17, 18, 25, 26,     // TP
				 21                          // B2B
				-> "Payment";
			case 33,  // RR
				 32,  // RD
				 98   // RD
				-> "Request";
			case 55 -> "Reject";  // RJ
			case 61, 62, 63, 64, 65, 66, 67, 68, 69,
				 71, 72, 73, 74, 75, 77, 82, 83, 85, 88
				-> "NACK";
			case 52 -> "Balance";  // Balance - for handling Balance Files i.e. HD:52
			default -> null;
		};
	}

	/**
	 * Builds XML from one line with enhanced error handling
	 * 
	 * @param inputValue the input value
	 * @throws Exception if processing fails
	 */
	private void process(String inputValue) throws Exception {
		if (inputValue == null || inputValue.length() < 2) {
			loggerBW.warn("Invalid input value: {}", inputValue);
			return;
		}
		
		String elementName = inputValue.substring(0, 2);
		atts.clear();
		
		try {
			Pattern pattern = Pattern.compile(getPatternValue(elementName + "_Pattern"));
			Matcher matcher = pattern.matcher(inputValue);
			
			if (!matcher.matches()) {
				loggerBW.warn("Pattern doesn't match for element {}: {}", elementName, inputValue);
				return;
			}
			
			th.startElement("", elementName, elementName, atts);
			
			// Get all groups for this match
			for (int i = 2; i <= matcher.groupCount(); i += 2) {
				String content = matcher.group(i);
				if (content != null && !content.isEmpty()) {
					String subElementName = elementName + (i / 2);
					th.startElement("", subElementName, subElementName, atts);
					th.characters(content.toCharArray(), 0, content.length());
					th.endElement("", subElementName, subElementName);
				}
			}
			
		} catch (SAXException e) {
			responseCode = "FAILURE";
			responseDescription = "Error during XML element creation: " + e.getMessage();
			loggerBW.error("Error creating XML elements", e);
		} catch (Exception e) {
			responseCode = "FAILURE";
			responseDescription = "Error during text parsing: " + e.getMessage() + "\n" + inputValue;
			loggerBW.error("Error parsing text: {}", inputValue, e);
		} finally {
			th.endElement("", elementName, elementName);
		}
	}

	/**
	 * Initializes XML message structure
	 * 
	 * @throws ParserConfigurationException if parser config fails
	 * @throws TransformerConfigurationException if transformer config fails
	 * @throws SAXException if SAX error occurs
	 */
	private void initXML() throws ParserConfigurationException,
			TransformerConfigurationException, SAXException {
		System.setProperty("javax.xml.transform.TransformerFactory",
				"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
		
		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		th = tf.newTransformerHandler();
		Transformer serializer = th.getTransformer();
		serializer.setOutputProperty(OutputKeys.MEDIA_TYPE, "XML");
		serializer.setOutputProperty(OutputKeys.ENCODING, getProperty(WRITER_ENCODING));
		serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");

		th.setResult(out);
		th.startDocument();
		
		atts = new AttributesImpl();
		atts.addAttribute("", "xmlns", "xmlns", "CDATA", MESSAGE_XML_NAMESPACE_URI);
		th.startElement("", "MESSAGE", "MESSAGE", atts);
		atts.addAttribute("", "xmlns", "xmlns", "CDATA", MSGSCERTIS_XML_NAMESPACE_URI);
		th.startElement("", "MSGSCERTIS", "MSGSCERTIS", atts);
		atts.clear();
	}

	/**
	 * Closes XML CERTIS result
	 * 
	 * @throws SAXException if SAX error occurs
	 */
	private void closeXML() throws SAXException {
		th.endElement("", "MSGSCERTIS", "MSGSCERTIS");
		
		addXmlElement("POSITION", String.valueOf(lineNumber));
		addXmlElement("EOF", String.valueOf(eof));
		addXmlElement("ResponseCode", responseCode);
		addXmlElement("ResponseDescription", responseDescription);
		
		th.endElement("", "MESSAGE", "MESSAGE");
		th.endDocument();
	}

	/**
	 * Helper method to add simple XML element
	 * 
	 * @param name element name
	 * @param value element value
	 * @throws SAXException if SAX error occurs
	 */
	private void addXmlElement(String name, String value) throws SAXException {
		th.startElement("", name, name, atts);
		th.characters(value.toCharArray(), 0, value.length());
		th.endElement("", name, name);
	}

	/**
	 * String left pad with spaces
	 * 
	 * @param str the string to pad
	 * @param size the target size
	 * @return padded string
	 */
	public String padStart(String str, int size) {
		return String.format("%" + size + "s", str);
	}
	
	public int getLineNumber() {
		return lineNumber;
	}

	public boolean isEof() {
		return eof;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public String getResponseDescription() {
		return responseDescription;
	}

	public static void main(String[] args) {
		CERTISToXML converter = new CERTISToXML();
		String[] result = converter.parseCertisFile(
				"C://Apoorva//Docs//CZ//JIRA_1369//F130723679_GEBA.510", 
				200, 
				0);
		
		System.out.println("XML Result:");
		System.out.println(result[0]);
		System.out.println("\nReject Messages:");
		System.out.println(result[1]);
	}
}

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

/**
 * To get the coordinates for a given location
 * @author Akshai
 *
 */
public class GetLocation {
	
	/**
	 * Calculates late long in 
	 * @param location
	 * @return
	 * @throws Exception
	 */
	public double[] returnLatLong(String location) throws Exception {
		String latLongs[] = getLatLongPositions(location);
		
		double[] returnValue = new double[2];
		returnValue[0] = Double.parseDouble(latLongs[0]);
		returnValue[1] = Double.parseDouble(latLongs[1]);
		return returnValue;
	}

	/**
	 * Get latitude and longitude for a given location
	 * @param location
	 * @return
	 */
	public static String[] getLatLongPositions(String location) {
		int responseCode = 0;
		try {
			String api = "http://maps.googleapis.com/maps/api/geocode/xml?address="
					+ URLEncoder.encode(location, "UTF-8") + "&sensor=true";

			URL url = new URL(api);
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.connect();
			responseCode = httpConnection.getResponseCode();
			if (responseCode == 200) {
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document document = builder.parse(httpConnection.getInputStream());
				XPathFactory xPathfactory = XPathFactory.newInstance();
				XPath xpath = xPathfactory.newXPath();
				XPathExpression expr = xpath.compile("/GeocodeResponse/status");
				String statusMessage = (String) expr.evaluate(document, XPathConstants.STRING);
				if (statusMessage.equals("OK")) {
					expr = xpath.compile("//geometry/location/lat");
					String[] latLong = new String[2];
					latLong[0] = (String) expr.evaluate(document, XPathConstants.STRING);
					expr = xpath.compile("//geometry/location/lng");
					latLong[1] = (String) expr.evaluate(document, XPathConstants.STRING);
					return latLong;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
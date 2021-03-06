package ca.efriesen.lydia.includes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.preference.PreferenceManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.google.android.gms.maps.model.LatLng;
import android.util.Log;

public class GMapV2Direction {
	private static final String TAG = "map directions";

	public final static int MODE_DRIVING = 0;
	public final static int MODE_WALKING = 1;
	public final static int MODE_BICYCLING = 2;
	public final static int MODE_TRANSIT = 3;

	public static ArrayList<String> modes = new ArrayList<String>();

	public static ArrayList<String> getModes() {
		// ensure we have our modes set
		setModes();
		return modes;
	}

	private static void setModes() {
		modes.clear();
		modes.add(MODE_DRIVING, "Driving");
		modes.add(MODE_WALKING, "Walking");
		modes.add(MODE_BICYCLING, "Bicycling");
		modes.add(MODE_TRANSIT, "Transit");
	}

	public GMapV2Direction() {
		setModes();
	}

	public Document getDocument(Context context, LatLng start, LatLng end, int mode) {
		// should we use metric or imperial?
		String unit = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("mapUseMetric", true) ? "metric" : "imperial";

		// make the url
		String url = "http://maps.googleapis.com/maps/api/directions/xml?" +
				"origin=" + start.latitude + "," + start.longitude + "&" +
				"destination=" + end.latitude + "," + end.longitude + "&" +
				"departure_time=" + System.currentTimeMillis()/1000 + "&" +
				"sensor=true&" +
				"units=" + unit + "&" +
				"mode=" + modes.get(mode).toLowerCase();
		// try to get the json from google
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			HttpPost httpPost = new HttpPost(url);
			HttpResponse response = httpClient.execute(httpPost, localContext);
			InputStream in = response.getEntity().getContent();
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(in);
			return doc;
		} catch (Exception e) {
			Log.e(TAG, "Get document error", e);
		}
		return null;
	}

	public String getDurationText (Document doc) {
		// this gets all instances of "duration".  we only want the last one, not the ones in the "step" elements
		NodeList nl1 = doc.getElementsByTagName("duration");
		Node node1 = nl1.item(nl1.getLength()-1);
		NodeList nl2 = node1.getChildNodes();
		Node node2 = nl2.item(getNodeIndex(nl2, "text"));
		Log.i("DurationText", node2.getTextContent());
		return node2.getTextContent();
	}

	public int getDurationValue (Document doc) {
		NodeList nl1 = doc.getElementsByTagName("duration");
		Node node1 = nl1.item(nl1.getLength()-1);
		NodeList nl2 = node1.getChildNodes();
		Node node2 = nl2.item(getNodeIndex(nl2, "value"));
		Log.i("DurationValue", node2.getTextContent());
		return Integer.parseInt(node2.getTextContent());
	}

	public String getDistanceText (Document doc) {
		NodeList nl1 = doc.getElementsByTagName("distance");
		Node node1 = nl1.item(nl1.getLength()-1);
		NodeList nl2 = node1.getChildNodes();
		Node node2 = nl2.item(getNodeIndex(nl2, "text"));
		Log.i("DistanceText", node2.getTextContent());
		return node2.getTextContent();
	}

	public int getDistanceValue (Document doc) {
		NodeList nl1 = doc.getElementsByTagName("distance");
		Node node1 = nl1.item(nl1.getLength()-1);
		NodeList nl2 = node1.getChildNodes();
		Node node2 = nl2.item(getNodeIndex(nl2, "value"));
		Log.i("DistanceValue", node2.getTextContent());
		return Integer.parseInt(node2.getTextContent());
	}

	public String getStartAddress (Document doc) {
		NodeList nl1 = doc.getElementsByTagName("start_address");
		Node node1 = nl1.item(0);
		Log.i("StartAddress", node1.getTextContent());
		return node1.getTextContent();
	}

	public String getEndAddress (Document doc) {
		NodeList nl1 = doc.getElementsByTagName("end_address");
		Node node1 = nl1.item(0);
		Log.i("StartAddress", node1.getTextContent());
		return node1.getTextContent();
	}

	public String getCopyRights (Document doc) {
		NodeList nl1 = doc.getElementsByTagName("copyrights");
		Node node1 = nl1.item(0);
		Log.i("CopyRights", node1.getTextContent());
		return node1.getTextContent();
	}

	public ArrayList<LatLng> getDirection (Document doc) {
		NodeList nl1, nl2, nl3;
		ArrayList<LatLng> listGeopoints = new ArrayList<LatLng>();
		nl1 = doc.getElementsByTagName("step");
		if (nl1.getLength() > 0) {
			for (int i = 0; i < nl1.getLength(); i++) {
				Node node1 = nl1.item(i);
				nl2 = node1.getChildNodes();

				Node locationNode = nl2.item(getNodeIndex(nl2, "start_location"));
				nl3 = locationNode.getChildNodes();
				Node latNode = nl3.item(getNodeIndex(nl3, "lat"));
				double lat = Double.parseDouble(latNode.getTextContent());
				Node lngNode = nl3.item(getNodeIndex(nl3, "lng"));
				double lng = Double.parseDouble(lngNode.getTextContent());
				listGeopoints.add(new LatLng(lat, lng));

				locationNode = nl2.item(getNodeIndex(nl2, "polyline"));
				nl3 = locationNode.getChildNodes();
				latNode = nl3.item(getNodeIndex(nl3, "points"));
				ArrayList<LatLng> arr = decodePoly(latNode.getTextContent());
				for(int j = 0 ; j < arr.size() ; j++) {
					listGeopoints.add(new LatLng(arr.get(j).latitude, arr.get(j).longitude));
				}

				locationNode = nl2.item(getNodeIndex(nl2, "end_location"));
				nl3 = locationNode.getChildNodes();
				latNode = nl3.item(getNodeIndex(nl3, "lat"));
				lat = Double.parseDouble(latNode.getTextContent());
				lngNode = nl3.item(getNodeIndex(nl3, "lng"));
				lng = Double.parseDouble(lngNode.getTextContent());
				listGeopoints.add(new LatLng(lat, lng));
			}
		}

		return listGeopoints;
	}

	public static Address getLocationFromAddress(Context context, String address) {
		try {
			Geocoder geocoder = new Geocoder(context);
			List<Address> addressList;
			addressList = geocoder.getFromLocationName(address, 5);
			return addressList.get(0);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private int getNodeIndex(NodeList nl, String nodename) {
		for(int i = 0 ; i < nl.getLength() ; i++) {
			if(nl.item(i).getNodeName().equals(nodename))
				return i;
		}
		return -1;
	}

	private ArrayList<LatLng> decodePoly(String encoded) {
		ArrayList<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;
		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;
			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
			poly.add(position);
		}
		return poly;
	}
}

package com.browserdriver.core;

import java.io.IOException;
import java.util.logging.Logger;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.PrintServiceAttribute;
import javax.print.attribute.standard.PrinterName;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.browserdriver.drivers.DigitalPersonaDriver;
import com.browserdriver.drivers.DigitalPersonaDriverEnroll;
import com.browserdriver.drivers.KojakDriver;
import com.browserdriver.stuff.PropertyLoader;
import com.integratedbiometrics.ibscanultimate.IBScanDevice;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.ImageType;

/**
 * @author GladOS.
 * The Enrichment Center reminds you that the Weighted Companion Cube
 * will never threaten to stab you and, in fact, cannot speak.
 * 
 * In the event that the weighted companion cube does speak, 
 * the Enrichment Center urges you to disregard its advice.
 */
@WebSocket()
public class WSHandler extends WebSocketHandler {

	private static final Logger LOGGER = Logger.getLogger(WSHandler.class.getName());
	public static String STATUS;

	private static KojakDriver kojakScanner;
	private static DigitalPersonaDriver dpDriver;
	private static DigitalPersonaDriverEnroll dpDriverEnroll;

	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
	}

	@OnWebSocketError
	public void onError(Throwable t) {
		LOGGER.severe(t.getMessage());
		t.printStackTrace();
	}

	@OnWebSocketConnect
	public void onConnect(Session session) {
	}

	@OnWebSocketMessage
	public void handleMessage(Session session, String message) throws IOException {
		LOGGER.info(message);
		switch (message) {
		case "ping":
			break;
		case "action:start:footPrint":
			streamScanner(session, IBScanDevice.ImageType.FLAT_FOUR_FINGERS, false);
			break;
		case "action:start:oneThumb":
			streamScanner(session, IBScanDevice.ImageType.FLAT_SINGLE_FINGER, true);
			break;
		case "action:start:twoThumbs":
			streamScanner(session, IBScanDevice.ImageType.FLAT_TWO_FINGERS, true);
			break;
		case "action:start:leftFour":
			streamScanner(session, IBScanDevice.ImageType.FLAT_FOUR_FINGERS, true);
			break;
		case "action:start:rightFour":
			streamScanner(session, IBScanDevice.ImageType.FLAT_FOUR_FINGERS, true);
			break;
		case "action:stop":
			kojakScanner.StopScanner();
			break;
		case "action:digitalpersona:capture":
			digitalPersonaCapture(session);
			break;
		case "action:digitalpersona:capture:enroll":
			digitalPersonaCaptureEnroll(session);
			break;
		default:
			try {
				JSONObject jon = new JSONObject(message);
				if (jon.getString("action").equals("list")) {
					session.getRemote().sendString(listPrinters(jon.getString("tipo")));
				}
				if (jon.getString("action").equals("print")) {
					printSomeShit(jon.getString("printer"), jon.getString("zpl"));
				}

				if (jon.getString("action").equals("PING")) {
					session.getRemote().sendString("la concha de tu madre all boys");
				}

			} catch (IOException e) {

				e.printStackTrace();
			} catch (JSONException e1) {

				e1.printStackTrace();
			}
		}

	}

	public void configure(WebSocketServletFactory factory) {
		// TODO Auto-generated method stub
		factory.register(WSHandler.class);
	}

	public String listPrinters(String tipo) {
		JSONArray jon = new JSONArray();
		JSONObject jo = new JSONObject();
		jo.put("tipo", tipo);
		System.out.println(tipo);
		PrintService psZebra = null;
		String sPrinterName = null;
		PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);

		for (int i = 0; i < services.length; i++) {

			PrintServiceAttribute attr = services[i].getAttribute(PrinterName.class);
			sPrinterName = ((PrinterName) attr).getValue();

			psZebra = services[i];

			if (tipo.equals("pulseras") && psZebra.toString().contains(PropertyLoader.getInstance().getPulserasKey()))
				jon.put(psZebra.getName());
			if (tipo.equals("etiquetas") && psZebra.toString().contains(PropertyLoader.getInstance().getEtiquetasKey()))
				jon.put(psZebra.getName());

		}

		if (psZebra == null) {
			jon.put("Not found.");
		}
		jo.put("printers", jon);
		return jo.toString();
	}

	private void printSomeShit(String printer, String zpl) {
		PrintService psZebra = null;
		String sPrinterName = null;
		PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);

		for (int i = 0; i < services.length; i++) {

			PrintServiceAttribute attr = services[i].getAttribute(PrinterName.class);
			sPrinterName = ((PrinterName) attr).getValue();

			if (sPrinterName.toLowerCase().equals(printer.toLowerCase())) {
				psZebra = services[i];

				break;
			}
		}
		if (psZebra == null) {
			LOGGER.severe("Error de impresion");

		} else {

			DocPrintJob job = psZebra.createPrintJob();
			byte[] by = zpl.getBytes();
			DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
			Doc doc = new SimpleDoc(by, flavor, null);
			try {
				job.print(doc, null);
				LOGGER.info("Impresion OK");
			} catch (PrintException e) {
				LOGGER.severe("Error de impresion");
			}
		}

	}

	public void streamScanner(Session session, ImageType imageType, boolean autoCapture) throws IOException {
		kojakScanner = new KojakDriver(session, imageType, autoCapture);
		kojakScanner.StartScanner();

	}

	public static void takeSnapshot() {
		kojakScanner.takeSnapshot();
		kojakScanner = null;
	}

	public void digitalPersonaCapture(Session session) {
		dpDriver = new DigitalPersonaDriver(session);

	}

	public void digitalPersonaCaptureEnroll(Session session) {
		dpDriverEnroll = new DigitalPersonaDriverEnroll(session);

	}
}
package com.browserdriver.core;

import java.awt.Color;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import com.browserdriver.stuff.MessageConsole;
import com.browserdriver.stuff.PropertyLoader;

public class InitBrowserDriver {
	private static PopupMenu popup;
	private static SystemTray systemTray;
	private static TrayIcon trayIcon;
	private static JFrame consoleframe;
	private static final Logger LOGGER = Logger.getLogger(InitBrowserDriver.class.getName());

	public static void mostrarMensajePulseraImpresa() {
		JOptionPane.showMessageDialog(null, "Pulsera Impresa");
	}

	public static void main(String[] args) {
		if (SystemTray.isSupported()) {

			String icoPath = "./cIcon.png";

			popup = new PopupMenu();
			systemTray = SystemTray.getSystemTray();
			trayIcon = new TrayIcon(new ImageIcon(icoPath, "omt").getImage(), "" + "localhost:40000");
			trayIcon.setImageAutoSize(true);

			JTextArea textComponent = new JTextArea();

			MessageConsole mc = new MessageConsole(textComponent);
			mc.redirectOut();
			mc.redirectErr(Color.RED, null);
			mc.setMessageLines(100000);

			consoleframe = new JFrame();
			consoleframe.add(new JScrollPane(textComponent));
			consoleframe.setSize(Toolkit.getDefaultToolkit().getScreenSize());

			MenuItem mnuSeeConsole = new MenuItem("Ver Consola");
			MenuItem mnuAboutItem = new MenuItem("Acerca de..");
			MenuItem mnuExit = new MenuItem("Salir");

			mnuSeeConsole.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					consoleframe.setVisible(true);
				}
			});

			mnuAboutItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(null, "Browser Driver V0.1");
				}
			});

			mnuExit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Object[] options = { "Acpetar", "Cancelar" };
					int n = JOptionPane.showOptionDialog(null,
							"Cerrar el Browser Driver hara imposible la comunicacion de la aplicacion de identificacion de pacientes con el hardware. Desea contunuar?",
							"Advertencia", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
							options[0]);

					if (n == 0)
						System.exit(0);
				}
			});

			// Add components to pop-up menu
			popup.add(mnuSeeConsole);
			popup.add(mnuAboutItem);
			popup.add(mnuExit);

			trayIcon.setPopupMenu(popup);

			
			

			try {
				systemTray.add(trayIcon);
				JOptionPane.showMessageDialog(null, "Browser Driver iniciado correctamente");
				// consoleframe.setVisible(true);

				 
				
				Server server = new Server();
				
				HttpConfiguration config = new HttpConfiguration();
			    config.setRequestHeaderSize(65535);
			    ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(config));
			    http.setPort(40000);
			 
			    server.setConnectors(new Connector[] {http});
				
			    
			    
	
				server.setHandler(new WSHandler());
				LOGGER.info("PULSRAS");
				LOGGER.info(new WSHandler().listPrinters("pulseras"));
				LOGGER.info("ETIQUETAS");
				LOGGER.info(new WSHandler().listPrinters("etiquetas"));

				server.start();
				LOGGER.info(PropertyLoader.getInstance().getStuff());
				LOGGER.info("lock and logged. Running@1.21gw on port 40000");
				LOGGER.info(System.getProperty("java.library.path"));
				server.join();

			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e.getMessage());
				LOGGER.severe(e.getMessage());

				e.printStackTrace();
			}finally {
				
			}
		}
	}

}

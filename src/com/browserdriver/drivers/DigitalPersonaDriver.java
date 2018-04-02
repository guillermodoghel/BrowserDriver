package com.browserdriver.drivers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.eclipse.jetty.websocket.api.Session;

import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPSample;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;

public class DigitalPersonaDriver {
	private Session session;
	private DPFPCapture capturer;
	private JFrame painInTheAssFrame;

	public DigitalPersonaDriver(Session s) {

		session = s;

		capturer = DPFPGlobal.getCaptureFactory().createCapture();
		capturer.addDataListener(new DPFPDataAdapter() {
			@Override
			public void dataAcquired(final DPFPDataEvent e) {
				System.out.println("The fingerprint sample was captured.");

				process(e.getSample());
				capturer.stopCapture();
				painInTheAssFrame.dispose();

			}
		});

		painInTheAssFrame = new JFrame("");
		painInTheAssFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		painInTheAssFrame.addWindowListener(new java.awt.event.WindowAdapter() {
	        @Override
	        public void windowClosing(java.awt.event.WindowEvent e) {
	          
	            try {
					session.getRemote().sendString("action:snapshot:result:cancel");
					capturer.stopCapture();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
	            e.getWindow().dispose();
	           
	        }
	    });
		
		painInTheAssFrame.setResizable(false);

		painInTheAssFrame.setBounds(100, 100, 450, 300);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));

		contentPane.setBackground(Color.WHITE);

		JLabel lblNewLabel_1 = new JLabel("CAPTURANDO HUELLA");
		contentPane.add(lblNewLabel_1, BorderLayout.NORTH);
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setFont(new Font("Lucida Grande", Font.BOLD, 20));

		JLabel lblNewLabel = new JLabel(new ImageIcon("./fingerLoad.gif"));
		lblNewLabel.setBackground(Color.WHITE);
		contentPane.add(lblNewLabel, BorderLayout.CENTER);
		painInTheAssFrame.setContentPane(contentPane);

		painInTheAssFrame.setLocationRelativeTo(null);
		painInTheAssFrame.setVisible(true);
		painInTheAssFrame.toFront();
		painInTheAssFrame.requestFocus();
		painInTheAssFrame.repaint();
		painInTheAssFrame.setAlwaysOnTop(true);

		try {
			capturer.startCapture();
		} catch (Exception e) {
			try {
				painInTheAssFrame.dispose();
				session.getRemote().sendString("action:snapshot:result:error");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
	

	}

	public void stop() {
		capturer.stopCapture();
	}

	protected void process(DPFPSample sample) {

		try {
			session.getRemote().sendString("action:snapshot:result:fullimage");
			session.getRemote()
					.sendBytes(ByteBuffer.wrap(BufferedImageToByte(toBufferedImage(convertSampleToBitmap(sample)))));

		} catch (IOException e) {
			System.out.println("pincho el coso");
			e.printStackTrace();
		}

	}

	private static byte[] imageInBytes;
	private static ByteArrayOutputStream baos;

	public static byte[] BufferedImageToByte(BufferedImage img) {
		imageInBytes = null;
		try {
			baos = new ByteArrayOutputStream();
			ImageIO.write(img, "jpg", baos);
			baos.flush();
			imageInBytes = baos.toByteArray();
			baos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return imageInBytes;
	}

	public static BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {

			return rotate((BufferedImage) img, 180);
		}

		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.rotate(180);
		bGr.dispose();

		// Return the buffered image
		return bimage;
	}

	public static GraphicsConfiguration getDefaultConfiguration() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		return gd.getDefaultConfiguration();
	}

	public static BufferedImage rotate(BufferedImage image, double angle) {
		int w = image.getWidth(), h = image.getHeight();
		GraphicsConfiguration gc = getDefaultConfiguration();
		BufferedImage result = gc.createCompatibleImage(w, h);
		Graphics2D g = result.createGraphics();
		g.rotate(Math.toRadians(angle), w / 2, h / 2);
		g.drawRenderedImage(image, null);
		g.dispose();
		return result;
	}

	protected Image convertSampleToBitmap(DPFPSample sample) {
		return DPFPGlobal.getSampleConversionFactory().createImage(sample);
	}

}

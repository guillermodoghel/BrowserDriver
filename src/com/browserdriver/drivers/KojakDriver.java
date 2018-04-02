package com.browserdriver.drivers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import org.eclipse.jetty.websocket.api.Session;

import com.integratedbiometrics.ibscancommon.IBCommon;
import com.integratedbiometrics.ibscanultimate.IBScan;
import com.integratedbiometrics.ibscanultimate.IBScanDevice;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.FingerCountState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.FingerQualityState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.ImageData;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.ImageType;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.PlatenState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.SegmentPosition;

import com.integratedbiometrics.ibscanultimate.IBScanDeviceListener;
import com.integratedbiometrics.ibscanultimate.IBScanException;
import com.integratedbiometrics.ibscanultimate.IBScanListener;

public class KojakDriver implements IBScanListener, IBScanDeviceListener {
	
	private static final Logger LOGGER = Logger.getLogger(KojakDriver.class.getName());
	private static final IBScanDevice.ImageResolution imageResolution = IBScanDevice.ImageResolution.RESOLUTION_500;


	private static Session session;
	
	private ImageType imageType;
	private boolean autoCapture;
	
	private static IBScan ibScan;
	private static IBScanDevice ibScanDevice;
	
	private static int captureOptions = 0;

	public KojakDriver(Session s, ImageType t, boolean auto) {
		session = s;
		this.imageType = t;
		this.autoCapture = auto;
	}


	public void StartScanner() {

		KojakDriver.ibScan = IBScan.getInstance();
		KojakDriver.ibScan.setScanListener(this);

		LOGGER.info("iniciandoo scan");
		try {
			if (KojakDriver.this.getIBScanDevice() == null) {
				KojakDriver.this.setIBScanDevice(KojakDriver.this.getIBScan().openDevice(0));
				LOGGER.info("IBScan.openDevice() successful 1");
			} else {
				if (!KojakDriver.this.getIBScanDevice().isOpened()) {
					KojakDriver.this.setIBScanDevice(KojakDriver.this.getIBScan().openDevice(0));
					LOGGER.info("IBScan.openDevice() successful 2");

				}
			}

			if (autoCapture) {
				captureOptions |= IBScanDevice.OPTION_AUTO_CAPTURE;
			}
			captureOptions |= IBScanDevice.OPTION_AUTO_CONTRAST;
		
			ibScanDevice.beginCaptureImage(imageType, imageResolution, captureOptions);
			LOGGER.info("iniciar captura de imagen");
		} catch (IBScanException ibse) {
			LOGGER.severe("IBScan.closeDevice() returned exception " + ibse.getType().toString() + ".");
			try {
				session.getRemote().sendString("error:iniciandoScanner:"+ibse.getType().toString());
			} catch (IOException e) {
				LOGGER.severe("error:returningError");
				e.printStackTrace();
			}
			try {
				ibScanDevice.cancelCaptureImage();
			} catch (IBScanException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				ibScanDevice.close();
			} catch (IBScanException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				IBScan.getInstance().unloadLibrary();
			} catch (IBScanException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			KojakDriver.ibScan = null;
			KojakDriver.ibScanDevice=null;
			ibse.printStackTrace();
			
		}

	}

	public void takeSnapshot() {

		try {

			getIBScanDevice().captureImageManually();

		} catch (IBScanException e) {
			e.printStackTrace();
		}
	}

	public void StopScanner() {
		try {
			ibScanDevice.cancelCaptureImage();

			LOGGER.info("cancelCaptureImage() OK");
		} catch (IBScanException e) {
			// TODO Auto-generated catch block
			LOGGER.info("cancelCaptureImage() ERROR");
			e.printStackTrace();
		}

	}

	@Override
	public void deviceAcquisitionBegun(IBScanDevice arg0, ImageType arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deviceAcquisitionCompleted(IBScanDevice arg0, ImageType arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deviceCommunicationBroken(IBScanDevice arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deviceFingerCountChanged(IBScanDevice arg0, FingerCountState arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deviceFingerQualityChanged(IBScanDevice arg0, FingerQualityState[] arg1) {
		// TODO Auto-generated method stub

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

	private static byte[] outImage;

	@Override
	public void deviceImagePreviewAvailable(IBScanDevice device, ImageData image) {
		try {
			session.getRemote().sendString("action:snapshot:simpleframe");
			session.getRemote().sendBytes(ByteBuffer.wrap(BufferedImageToByte(image.toSaveImage())));
		} catch (Exception e) {
			LOGGER.severe(e.getMessage());
			StopScanner();
		}
	}

	@Override
	public void deviceImageResultAvailable(IBScanDevice arg0, ImageData arg1, ImageType arg2, ImageData[] arg3) {
		LOGGER.info("deviceImageResultAvailable");
		try {
			KojakDriver.ibScanDevice.setBeeper(IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC, 2, 4, 0, 0);
		} catch (IBScanException e) {
			LOGGER.severe(e.getMessage());
			e.printStackTrace();
		}

	}

	@Override
	public void deviceImageResultExtendedAvailable(IBScanDevice device, IBScanException imageStatus, ImageData image,
			ImageType imageType, int detectedFingerCount, ImageData[] segmentImageArray,
			SegmentPosition[] segmentPositionArray) {


		try {

			session.getRemote().sendString("action:snapshot:result:fullimage");
			session.getRemote().sendBytes(ByteBuffer.wrap(BufferedImageToByte(image.toSaveImage())));

			session.getRemote().sendString("action:snapshot:result:segments:" + segmentImageArray.length);
			
//			Envio los cosos			
			for (int i = 0; i < segmentImageArray.length; i++) {
				session.getRemote().sendString("action:snapshot:result:segment:"+i);
				session.getRemote().sendBytes(ByteBuffer.wrap(BufferedImageToByte(segmentImageArray[i].toSaveImage())));
			}

			session.getRemote().sendString("action:nfiqScore:" + getIBScanDevice().calculateNfiqScore(image));

		} catch (IBScanException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			LOGGER.severe("IBScan.closeDevice() returned exception " + e1.getType().toString() + ".");

		} catch (IOException e) {
			e.printStackTrace();

		}

	}

	@Override
	public void devicePlatenStateChanged(IBScanDevice arg0, PlatenState arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void devicePressedKeyButtons(IBScanDevice arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deviceWarningReceived(IBScanDevice arg0, IBScanException arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scanDeviceCountChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scanDeviceInitProgress(int arg0, int arg1) {
		try {
			session.getRemote().sendString("action:opening:" + arg0 + ":" + arg1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void scanDeviceOpenComplete(int arg0, IBScanDevice arg1, IBScanException arg2) {
		try {
			session.getRemote().sendString("action:opening:complete");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/********************* GETTERS & SETTERS *****************************/

	// Get IBScan.
	protected IBScan getIBScan() {
		return KojakDriver.ibScan;
	}

	// Get opened or null IBScanDevice.
	protected IBScanDevice getIBScanDevice() {
		return KojakDriver.ibScanDevice;
	}

	// Set IBScanDevice.
	protected void setIBScanDevice(IBScanDevice ibScanDevice) {
		KojakDriver.ibScanDevice = ibScanDevice;
		if (ibScanDevice != null) {
			ibScanDevice.setScanDeviceListener(this);
		}
	}

}

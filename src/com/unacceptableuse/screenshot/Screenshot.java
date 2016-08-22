package com.unacceptableuse.screenshot;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.DisplayMode;
import java.awt.FileDialog;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;

import javax.imageio.ImageIO;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;

public class Screenshot implements ActionListener
{

	
	public static int SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width, SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
	public static final Encoder base = Base64.getEncoder();
	
	Robot robot;
	TrayIcon trayIcon;
	MenuItem lastLink;
	final SystemTray tray = SystemTray.getSystemTray();
	public int x = 0 , y= 0 , w=0, h=0;
	public BufferedImage TRAY_IDLE, TRAY_UPLOAD, TRAY_LINK;
	public AudioClip clip;
	public GraphicsDevice[] screenGraphics = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
	
	public Screenshot()
	{
		if(SystemTray.isSupported())
		{
			try
			{
				clip = Applet.newAudioClip(new File("uploaded.wav").toURI().toURL());
				TRAY_IDLE = ImageIO.read(getClass().getResourceAsStream("tray.png"));
				TRAY_UPLOAD = ImageIO.read(getClass().getResourceAsStream("tray_uploading_1.png"));
				TRAY_LINK = ImageIO.read(getClass().getResourceAsStream("tray_uploading_2.png"));
				final PopupMenu popup = new PopupMenu();
				trayIcon = new TrayIcon(TRAY_IDLE);
				trayIcon.setToolTip("UU Screencapper");
				lastLink = new MenuItem("No History");
				lastLink.setEnabled(false);
				lastLink.setActionCommand("recopyLink");
				MenuItem capClipboard = new MenuItem("Capture Clipboard");
				Menu capScreen = new Menu("Capture Screen");
				ArrayList<MenuItem> screens = new ArrayList<MenuItem>();
				for(int i = 0; i < screenGraphics.length; i++){
					 screens.add(new MenuItem("Screen "+i));
				}
				
				//MenuItem capWin = new MenuItem("Capture Active Window");
				MenuItem capAll = new MenuItem("Capture Everything");
				MenuItem capFile = new MenuItem("Upload File");
				MenuItem capSelection = new MenuItem("Capture Area");
				MenuItem exit = new MenuItem("Exit");
				
				
				popup.add(lastLink);
				popup.add(capClipboard);
				popup.add(capScreen);
				//popup.add(capWin);
				popup.add(capAll);
				popup.add(capFile);
				popup.add(capSelection);
				popup.add(exit);
				
				
				for(MenuItem m : screens){
					capScreen.add(m);
				}
				
				capScreen.addActionListener(this);
				popup.addActionListener(this);
				trayIcon.addActionListener(this);
				trayIcon.setPopupMenu(popup);
				tray.add(trayIcon);
				
				robot = new Robot();
						
			} catch (AWTException e)
			{
				new ErrorWindow("An error occurred creating the TrayIcon. Does your OS have a tray?", generateErrorOutput(e.getStackTrace()));
				e.printStackTrace();
			} catch (IOException e)
			{
				new ErrorWindow("An error occurred loading the resources. Peter forgot to pack the jar correctly.", generateErrorOutput(e.getStackTrace()));
				e.printStackTrace();
			}
		}else
		{
			new ErrorWindow("Your system does not appear to support Tray Icons. That's pretty fundemantal to this program.", "");
		}
	}
	
	public static String generateErrorOutput(StackTraceElement[] array){
		StringBuilder stb = new StringBuilder();
		for(StackTraceElement ste : array){
			stb.append(ste.toString()+"\n");
		}
		return stb.toString();
	}
	
	
	public static void main(String[] args)
	{
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		new Screenshot();
		
	}
	
	
	
	public Rectangle getActiveWindowBounds()
	{
		char[] buffer = new char[2048]; //Max window name size * 2
		HWND hwnd = User32.INSTANCE.GetForegroundWindow();
		User32.INSTANCE.GetWindowText(hwnd, buffer, 1024); //Max window name size
		RECT rect = new RECT();
		User32.INSTANCE.GetWindowRect(hwnd, rect);
		Rectangle shitRect = rect.toRectangle();
	    return new Rectangle(shitRect.x, shitRect.y, 1920, 1080);
	 }
	
	public Rectangle getFullBounds(){
		int width = 0;
		int height = 0;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		Rectangle bounds = new Rectangle();
		for (GraphicsDevice curGs : gs)
		{
		  DisplayMode mode = curGs.getDisplayMode();
		  width += mode.getWidth();
		  height = mode.getHeight();
		  bounds.add(curGs.getDefaultConfiguration().getBounds());
		}
		bounds.setSize(width, height);
		return bounds;
	}

	private void uploadScreenCapture(BufferedImage br){
		new Thread(new ThreadedScreenUpload(this, br), "Screenshot uploader thread").run();
	}
	
	private void uploadFile(File file){
		new Thread(new ThreadedFileUpload(this, file), "File uploader thread").run();
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		if(event.getActionCommand() == null)
		{		
			if(lastLink.getLabel().startsWith("http")){
				try {
					Desktop.getDesktop().browse(new URI(lastLink.getLabel()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}			
		}else
		if(event.getActionCommand().equals("Exit"))
		{
			System.exit(1);
		}else if(event.getActionCommand().equals("Capture Everything"))
		{
			uploadScreenCapture(robot.createScreenCapture(getFullBounds()));
		}else if(event.getActionCommand().equals("Capture Area"))
		{
			Overlay.launch(this);
			if(w > 0 && h > 0){
				uploadScreenCapture(robot.createScreenCapture(new Rectangle(x, y, w, h)));
				w = 0;
			}else if(w < 0 && h < 0){
				trayIcon.displayMessage("I'm a good programmer", "Please dont make backwards rectangles", MessageType.WARNING);
			}
		}else if(event.getActionCommand().equals("Capture Clipboard"))
		{
			Transferable clipboardImage = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);	
			if(clipboardImage != null && clipboardImage.isDataFlavorSupported(DataFlavor.imageFlavor))
			{
				try{
					for(DataFlavor df : clipboardImage.getTransferDataFlavors()){
						System.out.println(df.getClass());
					}
					uploadScreenCapture((BufferedImage)clipboardImage.getTransferData(DataFlavor.imageFlavor));
				}catch (UnsupportedFlavorException e)
			    {
					trayIcon.displayMessage("Upload Failed", "Clipboard contains an unsupported format. "+e.toString(), MessageType.ERROR);
					e.printStackTrace();
			    }
			    catch (IOException e)
			    {
			    	trayIcon.displayMessage("Upload Failed", "Error accessing clipboard. "+e.toString(), MessageType.ERROR);
			      e.printStackTrace();
			    }
			}else
			{
				trayIcon.displayMessage("Upload Failed", "Clipboard is empty or unsupported format.", MessageType.ERROR);
			}
			
			
		}else if(event.getActionCommand().equals("recopyLink"))
		{
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(lastLink.getLabel()), null);
		}else if(event.getActionCommand().equals("Capture Active Window"))
		{
			uploadScreenCapture(robot.createScreenCapture(getActiveWindowBounds()));
		}else if(event.getActionCommand().equals("Upload File")){
			FileDialog fd = new java.awt.FileDialog((java.awt.Frame) null);
			fd.setVisible(true);
			
			for(File file : fd.getFiles()){
				uploadFile(file);
			}
		}
		else if(event.getActionCommand().startsWith("Screen")){
			uploadScreenCapture(robot.createScreenCapture(screenGraphics[Integer.parseInt(event.getActionCommand().replace("Screen ",""))].getDefaultConfiguration().getBounds()));
		}else
		{
			System.err.println("Unknown action command: "+event.getActionCommand());
		}
		System.gc();
	}
}

package com.unacceptableuse.screenshot;

import java.awt.AWTException;
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
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Base64.Encoder;

import javax.imageio.ImageIO;

import com.sun.jna.Native;
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
	
	public Screenshot()
	{
		if(SystemTray.isSupported())
		{
			try
			{
				TRAY_IDLE = ImageIO.read(new File("tray.png"));
				TRAY_UPLOAD = ImageIO.read(new File("tray_uploading_1.png"));
				TRAY_LINK = ImageIO.read(new File("tray_uploading_2.png"));
				final PopupMenu popup = new PopupMenu();
				trayIcon = new TrayIcon(TRAY_IDLE);
				trayIcon.setToolTip("UU Screencapper");
				lastLink = new MenuItem("No History");
				lastLink.setEnabled(false);
				lastLink.setActionCommand("recopyLink");
				MenuItem capClipboard = new MenuItem("Capture Clipboard");
				Menu capScreen = new Menu("Capture Screen");
				MenuItem scr1 = new MenuItem("Left");
				MenuItem scr2 = new MenuItem("Center");
				MenuItem scr3 = new MenuItem("Right");
				//MenuItem capWin = new MenuItem("Capture Active Window");
				MenuItem capAll = new MenuItem("Capture Everything");
				MenuItem capSelection = new MenuItem("Capture Area");
				MenuItem exit = new MenuItem("Exit");
				
				
				popup.add(lastLink);
				popup.add(capClipboard);
				popup.add(capScreen);
				//popup.add(capWin);
				popup.add(capAll);
				popup.add(capSelection);
				popup.add(exit);
				
				capScreen.add(scr1);
				capScreen.add(scr2);
				capScreen.add(scr3);
				popup.addActionListener(this);
				trayIcon.addActionListener(this);
				trayIcon.setPopupMenu(popup);
				tray.add(trayIcon);
				
				robot = new Robot();
				
				
				
			} catch (AWTException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				
				e.printStackTrace();
			}
		}
	}
	
	
	public static void main(String[] args)
	{
		new Screenshot();
	}
	
	public void uploadScreenCapture(final BufferedImage br)
	{
		try{
			Thread.sleep(500);
			trayIcon.setImage(TRAY_UPLOAD);
			trayIcon.displayMessage("", "Uploading...", MessageType.INFO);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(br, "png", baos);
			baos.flush();
			String encodedImage = base.encodeToString(baos.toByteArray());
			baos.close();
			encodedImage = URLEncoder.encode(encodedImage, "ISO-8859-1");
			
			
			HttpURLConnection connection = (HttpURLConnection) new URL("http://files.unacceptableuse.com/post.php").openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			trayIcon.setImage(TRAY_LINK);
			OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
			osw.write("data="+encodedImage);
			osw.close();
			BufferedReader bir = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String link = bir.readLine();
			bir.close();
			connection.disconnect();
			
			encodedImage = null;
			connection = null;
			
			
			if(link.startsWith("http"))
			{
				lastLink.setEnabled(true);
				lastLink.setLabel(link);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(link), null);
				trayIcon.displayMessage("Screenshot Uploaded" ,link, MessageType.INFO);
			}else
				trayIcon.displayMessage("Upload Failed", link, MessageType.ERROR);

		}catch(Exception e)
		{
			trayIcon.displayMessage("Upload Failed", e.toString(), MessageType.ERROR);
		}
		trayIcon.setImage(TRAY_IDLE);
	}
	
	public Rectangle getActiveWindowBounds()
	{
		char[] buffer = new char[2048]; //Max window name size * 2
		HWND hwnd = User32.INSTANCE.GetForegroundWindow();
		User32.INSTANCE.GetWindowText(hwnd, buffer, 1024); //Max window name size
		RECT rect = new RECT();
		User32.INSTANCE.GetWindowRect(hwnd, rect);
	    return rect.toRectangle();
	 }

	

	@Override
	public void actionPerformed(ActionEvent event)
	{
		if(event.getActionCommand().equals("Exit"))
		{
			System.exit(1);
		}else if(event.getActionCommand().equals("Capture Everything"))
		{
			uploadScreenCapture(robot.createScreenCapture(new Rectangle(-SCREEN_WIDTH, 0, SCREEN_WIDTH*3, SCREEN_HEIGHT)));
		}else if(event.getActionCommand().equals("Capture Area"))
		{
			Overlay.launch(this);
			if(w > 0 && h > 0){
				uploadScreenCapture(robot.createScreenCapture(new Rectangle(x, y, w, h)));
				w = 0;
			}
		}else if(event.getActionCommand().equals("Center"))
		{
			uploadScreenCapture(robot.createScreenCapture(new Rectangle(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT)));
		}else if(event.getActionCommand().equals("Left"))
		{
			uploadScreenCapture(robot.createScreenCapture(new Rectangle(-SCREEN_WIDTH, 0, SCREEN_WIDTH, SCREEN_HEIGHT)));
		}else if(event.getActionCommand().equals("Right"))
		{
			uploadScreenCapture(robot.createScreenCapture(new Rectangle(SCREEN_WIDTH, 0, SCREEN_WIDTH, SCREEN_HEIGHT)));
		}else if(event.getActionCommand().equals("Capture Clipboard"))
		{
			Transferable clipboardImage = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);	
			if(clipboardImage != null && clipboardImage.isDataFlavorSupported(DataFlavor.imageFlavor))
			{
				try{
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
		}
		else
		{
			System.err.println("Unknown action command: "+event.getActionCommand());
		}
	}
}

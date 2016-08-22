package com.unacceptableuse.screenshot;

import java.awt.Toolkit;
import java.awt.TrayIcon.MessageType;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class ThreadedScreenUpload implements Runnable{

	private Screenshot parent;
	private BufferedImage br;
	
	public ThreadedScreenUpload(Screenshot parent, BufferedImage br){
		this.parent = parent;
		this.br = br;
		
	}

	@Override
	public void run() {
		uploadScreenCapture(br);
	}
	
	
	public void uploadScreenCapture(final BufferedImage br)
	{
		try{
			parent.trayIcon.setImage(parent.TRAY_UPLOAD);
			//parent.trayIcon.displayMessage("", "Uploading...", MessageType.INFO);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(br, "png", baos);
			baos.flush();
			String encodedImage = parent.base.encodeToString(baos.toByteArray());
			baos.close();
			encodedImage = URLEncoder.encode(encodedImage, "ISO-8859-1");
			
			
			HttpURLConnection connection = (HttpURLConnection) new URL("http://files.unacceptableuse.com/post.php").openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			parent.trayIcon.setImage(parent.TRAY_LINK);
			OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
			osw.write("filetype=png&");
			osw.write("data="+encodedImage);
			osw.close();
			BufferedReader bir = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String link = bir.readLine();
			bir.close();
			connection.disconnect();
			
			encodedImage = null;
			connection = null;
			Runtime.getRuntime().gc();
			
			
			if(link.startsWith("http"))
			{
				parent.lastLink.setEnabled(true);
				parent.lastLink.setLabel(link);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(link), null);
				parent.trayIcon.displayMessage("Screenshot Uploaded" ,link, MessageType.INFO);
				parent.clip.play();
			}else{
				parent.trayIcon.displayMessage("Upload Failed", link, MessageType.ERROR);
				System.out.println(link);
			}
				

		}catch(Exception e)
		{
			parent.trayIcon.displayMessage("Upload Failed", e.toString(), MessageType.ERROR);
			e.printStackTrace();
		}
		parent.trayIcon.setImage(parent.TRAY_IDLE);
	}
}

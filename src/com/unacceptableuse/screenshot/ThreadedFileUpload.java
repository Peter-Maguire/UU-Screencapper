package com.unacceptableuse.screenshot;

import java.awt.Toolkit;
import java.awt.TrayIcon.MessageType;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ThreadedFileUpload implements Runnable{

	Screenshot parent;
	File file;
	
	public ThreadedFileUpload(Screenshot parent, File file){
		this.parent = parent;
		this.file = file;
				
	}
	
	@Override
	public void run() {
		upload(file);		
	}
	
	
	public void upload(File file){
		try{
			parent.trayIcon.setImage(parent.TRAY_UPLOAD);
			parent.trayIcon.displayMessage("", "Uploading...", MessageType.INFO);
			
			long length = file.length();
			if(length > Integer.MAX_VALUE){
				parent.trayIcon.displayMessage("File too large", "The file "+file.getName()+" is too large to upload.", MessageType.ERROR);
			}else{
				InputStream is = new FileInputStream(file);
				
				byte[] bytes = new byte[(int)length];
				int offset = 0;
				int numRead = 0;
				while(offset < bytes.length&& (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0){
					offset+=numRead;
				}
				
				if(offset < bytes.length){
					parent.trayIcon.displayMessage("File read error", "The file "+file.getName()+" couldn't be fully read.", MessageType.ERROR);
				}else{
					
					is.close();
					
					Runtime.getRuntime().gc();
					String encodedFile = parent.base.encodeToString(bytes);
					encodedFile = URLEncoder.encode(encodedFile, "ISO-8859-1");
					HttpURLConnection connection = (HttpURLConnection) new URL("http://files.unacceptableuse.com/post.php").openConnection();
					connection.setDoOutput(true);
					connection.setDoInput(true);
					connection.setRequestMethod("POST");
					parent.trayIcon.setImage(parent.TRAY_LINK);
					OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
					osw.write("filetype="+file.getName().substring(file.getName().lastIndexOf(".")+1)+"&");
					osw.write("data="+encodedFile);
					osw.close();
					BufferedReader bir = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					String link = bir.readLine();
					bir.close();
					connection.disconnect();
					
					encodedFile = null;
					connection = null;
					
					
					if(link.startsWith("http"))
					{
						parent.lastLink.setEnabled(true);
						parent.lastLink.setLabel(link);
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(link), null);
						parent.trayIcon.displayMessage("File Uploaded" ,link, MessageType.INFO);
						parent.clip.play();
					}else
						parent.trayIcon.displayMessage("Upload Failed", link, MessageType.ERROR);
				}
			}
		}catch(Exception e)
		{
			parent.trayIcon.displayMessage("Upload Failed", e.toString(), MessageType.ERROR);
			e.printStackTrace();
		}
		parent.trayIcon.setImage(parent.TRAY_IDLE);
	}

}

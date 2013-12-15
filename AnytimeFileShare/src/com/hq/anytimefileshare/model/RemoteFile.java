package com.hq.anytimefileshare.model;


import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;


import android.util.Log;

import com.hq.anytimefileshare.Global;
import com.hq.anytimefileshare.model.dao.FileInfo;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

public class RemoteFile extends SmbFile {
	public RemoteFile(String url) throws MalformedURLException {
		super(url);
		
		Log.d("RemoteFile", "remote uri:" + getPath());
	}
	
	
	public ArrayList<FileInfo> getFileInfo() throws Exception {
		ArrayList<FileInfo> list = new ArrayList<FileInfo>();
		
		try {
			SmbFile[] files = listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					FileInfo f = new FileInfo();
					
					f.setDirectory(files[i].isDirectory());
					String fileName = null;
					if (files[i].isDirectory()) {
						//如果是目录去掉最后的“/”
						fileName = files[i].getName().substring(0, files[i].getName().length() - 1);
					} else {
						fileName = files[i].getName();
					}
					
					f.setFileName(fileName);
					list.add(f);
				}
			}
			
		} catch (Exception e) {
			Log.e("RemoteFile", "Get file information exception:" + e.getMessage());
			throw e;
			//e.printStackTrace();
		}
		//Collections.sort(list);

        return list; 
	}
	
	void WriteLocalFile(LocalFile file, SmbFile smbFile, ModelProcess p, int maxStep) throws Exception {		
		BufferedInputStream bufIn = new BufferedInputStream(new SmbFileInputStream(smbFile));		
		byte[] buffer = new byte[Contants.MAX_BUFFER_LEN];		
		int readLen;
	    int loopCountToStep = 1;
		
		try {
			file.createOutFile();
			if (smbFile.length() > 0) {
			    int step = (int) (Contants.MAX_BUFFER_LEN * maxStep / smbFile.length());
				if (step == 0) {
					float f = ((float)(Contants.MAX_BUFFER_LEN * maxStep)) / (float)smbFile.length();
					step = 1;
					loopCountToStep = (int) (step / f);
				}
				
				int tmpCount = 1;
				while ((readLen = bufIn.read(buffer)) != -1) {				
					file.write(buffer, readLen);
					if ((tmpCount % loopCountToStep) == 0) {
						p.copyToLocalIncProcess(step);
					}
					tmpCount++;
				}
			}
		} catch (Exception e) {
			Log.e("RemoteFile", "WriteLocalFile exception:" + e.getMessage());
			throw e;
		} finally {
			file.close();
			bufIn.close();
		}
	}
	
	public void copyToLocal(String localPath, ModelProcess p, int maxStep) throws Exception {		
		int step = maxStep;
		
		try {
			LocalFile localFile = new LocalFile(localPath + "/" + getName());
			if (isDirectory()) {
				localFile.mkdirs();
				Log.d("RemoteFile", "Local folder:" + localFile.getPath() + " was build");
				
				SmbFile[] files = listFiles();
				if (files == null) {
					Log.e("RemoteFile", "Remote isn't find files.");
					return;
				}
				
				if (files.length > 0) {
					step = maxStep / files.length;
				}
				
				for (int i = 0; i < files.length; i++) {
					Log.d("RemoteFile", "RemoteFile:" + files[i].getName() + ",file type is:" + files[i].isDirectory());
					
					if (files[i].isDirectory()) {
						RemoteFile subFile = new RemoteFile(getPath() + files[i].getName());
						subFile.copyToLocal(localFile.getPath(), p, step);
					} else {
						LocalFile subLocalFile = new LocalFile(localFile.getPath() + "/" + files[i].getName());
						WriteLocalFile(subLocalFile, files[i], 
										p, step);			
					}
					
					p.copyToLocalIncProcess(step);	
				}									
			} else {	
				WriteLocalFile(localFile, this, p, maxStep);
			}			
			p.copyToLocalIncProcess(maxStep);
		} catch (SmbException e) {
			e.printStackTrace();
			Log.e("RemoteFile", "copyToLocal Smb exception:" + e.getMessage());
			throw e;
		} catch (MalformedURLException e) {
			//e.printStackTrace();
			Log.e("RemoteFile", "copyToLocal URL exception:" + e.getMessage());
			throw e;
		} catch (IOException e) {
			//throw e;
			//e.printStackTrace();
			Log.e("RemoteFile", "copyToLocal IO exception:" + e.getMessage());
			throw e;
		} catch (Exception e) {
			Log.e("RemoteFile", "copyToLocal exception:" + e.getMessage());
			throw e;
		}
	}

}

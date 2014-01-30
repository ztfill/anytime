package com.hq.anytimefileshare.model;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;

import android.util.Log;

import com.hq.anytimefileshare.model.dao.FileInfo;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

public class RemoteFile extends FileBase {
	SmbFile mSmbFile = null;	
	
	static {
		System.setProperty("jcifs.smb.client.dfs.disabled", "true");
	}
	
	public RemoteFile(String pathName) throws Exception {
		//super(url);
		mPathName = pathName;
		try {
			mSmbFile = new SmbFile(mPathName);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		Log.d("RemoteFile", "remote uri:" + mPathName);
	}
		
	public boolean isDirectory() throws Exception {
		try {
			return mSmbFile.isDirectory();
		} catch (SmbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}
	
	
	public void initWriteOutStream() throws Exception {
		try {
			mOut = new BufferedOutputStream(new SmbFileOutputStream(mSmbFile));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("", "create file fail:" + e.getMessage());
			throw e;
		}
	}
	
	public void initReadInStream() throws Exception {
		mIn = new BufferedInputStream(new SmbFileInputStream(mSmbFile));
	}
			
	public void write(FileBase fb) throws Exception {
		byte[] b = new byte[Contants.MAX_BUFFER_LEN];
		
		try {
		if (fb.isDirectory()) {
			ArrayList<FileInfo> list = fb.getFileList();
			Iterator<FileInfo> it = list.iterator();
			while (it.hasNext()) {
				FileInfo f = it.next();
				FileBase another = fb.setAnotherFile(f.getFileName());
				RemoteFile rf = new RemoteFile(mPathName + FILE_DIRECTORY_SPLITE_LABLE + another.getFileName());
				rf.write(another);
			}
		} else {		
			int readLen;
			while ((readLen = fb.read(b)) != -1) {				
				write(b, readLen);
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			fb.close();
			close();
		}
	}
	
	protected ArrayList<FileInfo> getFileList() throws Exception {
		ArrayList<FileInfo> list = new ArrayList<FileInfo>();
		
		try {
			SmbFile[] sf = mSmbFile.listFiles();
			if (sf == null) {
				return null;
			}
			
			for (int i = 0; i < sf.length; i++) {
				FileInfo f = new FileInfo();
				f.setDirectory(sf[i].isDirectory());
				f.setFileName(sf[i].getPath());
				list.add(f);
			}
			
		} catch (SmbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}		
		
		return list;
	}
	
	protected RemoteFile setAnotherFile(String pathName) throws Exception {
		return new RemoteFile(pathName);
	}
	
	
	
	public ArrayList<FileInfo> getFileInfo() throws Exception {
		ArrayList<FileInfo> list = new ArrayList<FileInfo>();
		
		try {
			SmbFile[] files = mSmbFile.listFiles();
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
	/*	
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
*/
}

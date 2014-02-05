package com.hq.anytimefileshare.model;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;

import android.util.Log;

import com.hq.anytimefileshare.Global;
import com.hq.anytimefileshare.model.dao.FileInfo;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

public class RemoteFile extends FileBase {
	private SmbFile mSmbFile = null;	
	private String SMB_URI_LABEL = "smb://";
	
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
	
	long getFileLen() throws Exception {
		return mSmbFile.length();
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
	
	protected ArrayList<FileInfo> getFileList() throws Exception {
		ArrayList<FileInfo> list = new ArrayList<FileInfo>();
		
		try {
			SmbFile[] sf = mSmbFile.listFiles();
			if (sf != null) {
				for (int i = 0; i < sf.length; i++) {
					FileInfo f = new FileInfo();
					f.setDirectory(sf[i].isDirectory());
					f.setFileName(sf[i].getPath());
					list.add(f);
				}
			}			
		} catch (SmbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}		
		
		return list;
	}
	
	public FileBase getNewFileInstance(String pathName) throws Exception {
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
	
	void mkdirs() throws Exception {		
		try {
			mSmbFile.mkdirs();
		} catch (Exception e) {
			Log.e("LocalFile", "mkdirs  exception:" + e.getMessage());
			throw e;
		}
	}
	
	public String getShowPath() {
		String str = null;			
		
		try {
			str = mSmbFile.getPath();
			String[] arrayStr = str.split("@");
			if (arrayStr.length > 1) {
				str = FILE_DIRECTORY_SPLITE_LABLE + arrayStr[1];
			} else {
				str = str.substring(SMB_URI_LABEL.length());
				str = FILE_DIRECTORY_SPLITE_LABLE + str;
				str = str.substring(0, str.length() - 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("getShowPath", "Get remote path fail:" + e.getMessage());
			str = null;
		}				
		
		return str;
	}
	
	public String getParent() {
		return mSmbFile.getParent();
	}
	
}

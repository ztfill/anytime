package com.hq.anytimefileshare.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;


import android.util.Log;

import com.hq.anytimefileshare.model.dao.FileInfo;

public class LocalFile extends FileBase {
	File mFile;
	BufferedOutputStream mBufOut = null;

	public LocalFile(String pathName) {
		mPathName = pathName;
		mFile = new File(pathName);
		Log.d("LocalFile", "Local uri:" + mPathName);
	}
	
	public void initReadInStream() throws Exception {
		mIn = new BufferedInputStream(new FileInputStream(mFile));
	}
	public void initWriteOutStream() throws Exception {
		mOut = new BufferedOutputStream(new FileOutputStream(mFile));
	}
	
	public void write(FileBase fb) throws Exception {
		
	}
	
	public boolean isDirectory() throws Exception {
		try {
			return mFile.isDirectory();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}
	ArrayList<FileInfo> getFileList() throws Exception {		
		ArrayList<FileInfo> list = null;
		
		try {
			File[] sf = mFile.listFiles();
			if (sf == null) {
				return null;
			}
			
			for (int i = 0; i < sf.length; i++) {
				FileInfo f = new FileInfo();
				f.setDirectory(sf[i].isDirectory());
				f.setFileName(sf[i].getPath());
			}
			
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}		
		
		return list;
	}
	
	FileBase setAnotherFile(String pathName) throws Exception {
		return new LocalFile(pathName);
	}
	
	public ArrayList<FileInfo> getFileInfo() throws Exception {
		ArrayList<FileInfo> list = new ArrayList<FileInfo>();
		
		try {
			File[] files = mFile.listFiles();
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
	
	public void createOutFile() throws FileNotFoundException {
		mBufOut = new BufferedOutputStream(new FileOutputStream(getPath()));
	}
	
	public void write(byte[] b, int writeLen) throws IOException {
		mBufOut.write(b, 0, writeLen);
	}
	
	public void close() {
		try {
			if (mBufOut != null) {
				mBufOut.flush();
				mBufOut.close();
				mBufOut = null;
			}
		} catch (IOException e) {
			Log.e("LocalFile", "Close file fail.");
		}
		
		
	}
	
	public void getLocalFileInfo(ArrayList<FileInfo> list) {
		File[] files = listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				FileInfo f = new FileInfo();
				
				f.setDirectory(files[i].isDirectory());
				f.setFileName(files[i].getName());
				list.add(f);
			}
		}
	}
	
	public ArrayList<FileInfo> getLocalFileInfo() {
		ArrayList<FileInfo> list = new ArrayList<FileInfo>();
		
		getLocalFileInfo(list);		
		return list;
	}
*/
}

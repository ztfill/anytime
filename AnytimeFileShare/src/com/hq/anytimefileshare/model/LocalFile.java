package com.hq.anytimefileshare.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.util.Log;

import com.hq.anytimefileshare.model.dao.FileInfo;

public class LocalFile extends FileBase {
	private File mFile;

	public LocalFile(String pathName) throws Exception {
		mFile = new File(pathName);
		mPathName = pathName;
		try {
			if (isDirectory() 
				&& (mPathName.lastIndexOf(FILE_DIRECTORY_SPLITE_LABLE) != (mPathName.length() - 1))) {
				mPathName += "/";
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("LocalFile", "Get file Directory info exception:" + e.getMessage());
			throw e;
		}
		Log.d("LocalFile", "Local uri:" + mPathName);
	}
	
	long getFileLen() throws Exception {
		return mFile.length();
	}
	
	public String getShowPath() {
		return mFile.getPath();
	}
	
	public String getParent() {
		return mFile.getParent();
	}
	
	public void initReadInStream() throws Exception {
		mIn = new BufferedInputStream(new FileInputStream(mFile));
	}
	public void initWriteOutStream() throws Exception {
		mOut = new BufferedOutputStream(new FileOutputStream(mFile));
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
		ArrayList<FileInfo> list = new ArrayList<FileInfo>();
		
		try {
			File[] sf = mFile.listFiles();
			if (sf != null) {
				for (int i = 0; i < sf.length; i++) {
					FileInfo f = new FileInfo();
					f.setDirectory(sf[i].isDirectory());
					f.setFileName(sf[i].getPath());
					
					list.add(f);
				}
			}
		} catch (SecurityException e) {
			e.printStackTrace();
			throw e;
		}		
		
		return list;
	}
	
	public FileBase getNewFileInstance(String pathName) throws Exception {
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
					fileName = files[i].getName();
									
					f.setFileName(fileName);
					list.add(f);
				}
			}
			
		} catch (Exception e) {
			Log.e("LocalFile", "Get file information exception:" + e.getMessage());
			throw e;
			//e.printStackTrace();
		}
		//Collections.sort(list);

        return list; 
	}
	
	void mkdirs() throws Exception {		
		try {
			mFile.mkdirs();
		} catch (Exception e) {
			Log.e("LocalFile", "mkdirs exception:" + e.getMessage());
			throw e;
		}
	}
	
}

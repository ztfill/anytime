package com.hq.anytimefileshare.model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


import android.util.Log;

import com.hq.anytimefileshare.model.dao.FileInfo;

public class LocalFile extends File {
	BufferedOutputStream mBufOut = null;

	public LocalFile(String path) {
		super(path);		
	}
	
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

}

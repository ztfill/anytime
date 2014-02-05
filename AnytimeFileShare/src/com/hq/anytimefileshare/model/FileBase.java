package com.hq.anytimefileshare.model;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import android.util.Log;

import com.hq.anytimefileshare.model.dao.FileInfo;

public abstract class FileBase {	
	String mPathName = null;
	OutputStream mOut = null;
	InputStream mIn = null;
	public static final String FILE_DIRECTORY_SPLITE_LABLE = "/";
	
	abstract long getFileLen() throws Exception;
	abstract void initReadInStream() throws Exception;
	abstract void initWriteOutStream() throws Exception;
	abstract ArrayList<FileInfo> getFileList() throws Exception;
	abstract void mkdirs() throws Exception;
	public abstract FileBase getNewFileInstance(String fileName) throws Exception;
	public abstract boolean isDirectory() throws Exception;
	public abstract ArrayList<FileInfo> getFileInfo() throws Exception;
	public abstract String getShowPath();
	public abstract String getParent();
	public String getPath() throws Exception {
		return mPathName;
	}
	
	
	public void close() {
		try {
			if (mOut != null) {
				mOut.flush();
				mOut.close();
				mOut = null;
			}
			if (mIn != null) {
				mIn.close();
				mIn = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public void write(byte[] b, int writeLen) throws Exception {	
		if (mOut == null) {
			initWriteOutStream();
		}
		mOut.write(b, 0, writeLen);
		
	}
	
	public void write(FileBase fb, FileProgress fp, int maxStep) throws Exception {
		byte[] b = new byte[Contants.MAX_BUFFER_LEN];
		int step;
		
		try {
			if (fb.isDirectory()) {
				mkdirs();
				ArrayList<FileInfo> list = fb.getFileList();
				if (list.size() > 0) {
					step = maxStep / list.size();				
					Iterator<FileInfo> it = list.iterator();
					while (it.hasNext()) {
						FileInfo f = it.next();
						FileBase another = fb.getNewFileInstance(f.getFileName());
						//RemoteFile rf = new RemoteFile(mPathName + FILE_DIRECTORY_SPLITE_LABLE + another.getFileName());
						FileBase rf = getNewFileInstance(mPathName + FILE_DIRECTORY_SPLITE_LABLE + another.getFileName());
						rf.write(another, fp, step);
					}
				}
			} else {	
				int loopCountToStep = 1;
				
				if (getFileLen() > 0) {
					step = (int) (Contants.MAX_BUFFER_LEN * maxStep / getFileLen());
				} else {
					step = maxStep;
				}
				if (step == 0) {
					float f = ((float)(Contants.MAX_BUFFER_LEN * maxStep)) / (float)getFileLen();
					step = 1;
					loopCountToStep = (int) (step / f);
				}
				
				int readLen;
				int tmpCount = 1;
				long incBytes = 0;
				while ((readLen = fb.read(b)) != -1) {				
					write(b, readLen);
					incBytes += readLen;
					if ((tmpCount % loopCountToStep) == 0) {
						fp.writeProgress(step, incBytes);
						incBytes = 0;
					}
					tmpCount++;
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
	
	public int read(byte[] b) throws Exception {
		if (mIn == null) {
			initReadInStream();
		}
		return mIn.read(b);
	}
	
	public String getFileName() {
		String str = null;
		int index = mPathName.lastIndexOf(FILE_DIRECTORY_SPLITE_LABLE);
		
		if (index == (mPathName.length() - 1)) {
			String[] s = mPathName.split(FILE_DIRECTORY_SPLITE_LABLE);
			 str = s[s.length - 1] + FILE_DIRECTORY_SPLITE_LABLE;
		} else {
			str = mPathName.substring(index + 1);	
		}
		return str;
	}
}

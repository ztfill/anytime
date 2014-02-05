package com.hq.anytimefileshare;

import java.util.ArrayList;

import com.hq.anytimefileshare.model.FileBase;

import android.support.v4.app.Fragment;


public final class Global {
	static final String LOCAL_KEY_URI = "local_uri";
	static final String REMOTE_KEY_URI = "remote_uri";
	//static final String REMOTE_FILE_LIST = "remote_file_list";
	static final String REMOTE_URI_LABEL = "smb://";
	static final String DIRECTORY_SPLITE_LABLE = "/";
	static final short PROMPT_TIME = 2000;
	static final short PROGRESS_MAX = 100;
	
	
	static final String HANDLER_RESULT = "result";
	static final String HANDLER_MSG = "msg";
	static final String HANDLE_STEP = "process_step";
	
	static final int ERRNO_SUCCESS = 0;
	static final int ERRNO_FAIL = 1;
	static final int ERRNO_PROCESS = 3;
	
	private static String gRemoteUri = null;
	private static String gClipboardPath = null;
	
	private static ArrayList<FileBase> gClipboardFileList = new ArrayList<FileBase>();

	private static ArrayList<String> gFileNameList = null;
	
	private static FragmentBase gCurrentFrag = null;
	
	
	public static String getRemoteUri() {
		return gRemoteUri;
	}
	public static void setRemoteUri(String uri) {
		Global.gRemoteUri = uri;
	}
	public static ArrayList<String> getFileNameList() {
		return gFileNameList;
	}
	public static void setFileNameList(ArrayList<String> gFileNameList) {
		Global.gFileNameList = gFileNameList;
	}
	
	public static String getgClipboardPath() {
		return gClipboardPath;
	}
	public static void setgClipboardPath(String gClipboardPath) {
		Global.gClipboardPath = gClipboardPath;
	}
	public static ArrayList<FileBase> getClipboardFileList() {
		return gClipboardFileList;
	}
	public static void addFileToClipboardFileList(FileBase file) {
		Global.gClipboardFileList.add(file);
	}

	public static void delFileFromClipboardFileList(FileBase file) {
		Global.gClipboardFileList.remove(file);
	}
	
	public static void removeAllClipboardFile() {
		Global.gClipboardFileList.clear();
	}
	public static FragmentBase getgCurrentFrag() {
		return gCurrentFrag;
	}
	public static void setgCurrentFrag(FragmentBase gCurrentFrag) {
		Global.gCurrentFrag = gCurrentFrag;
	}
}

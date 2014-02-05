package com.hq.anytimefileshare.model;

public interface FileProgress {
	public void writeProgress(int step, long incBytes);
}

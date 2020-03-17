package com.linkage.ftpdrudgery.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WatchThread extends Thread{
	private Process p;
	private boolean over;
	private List<String> stream;
	
	public WatchThread(Process p){
		this.p = p;
		over = false;
		stream = new ArrayList<String>();
	}
	
	public void run(){
		if(p == null){
			return;
		}
		Scanner br = new Scanner(p.getInputStream());
		while(true){
			if( p == null || over){
				break;
			}
			while(br.hasNextLine()){
				String tempStream = br.nextLine();
				if(tempStream.trim() == null || tempStream.trim().equals("")){
					continue;
				}
				stream.add(tempStream);
			}
		}
	}

	public void setOver(boolean over) {
		this.over = over;
	}

	public List<String> getStream() {
		return stream;
	}

	
}

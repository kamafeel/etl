package com.linkage.ftpdrudgery.thread;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StartServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5329362481153793746L;

	/**
	 * Constructor of the object.
	 */
	public StartServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
		System.out.println("~~~~~~~~~~~~~~~GIS¼à¿Ø¡«¡«¡«¡«¡«¡«¡«¡«¡«");
		this.initPara();
		this.initThread();
	}
	
	private void initPara(){
		PartdisBean.getInstance().setPartPath(this.getInitParameter("PART_PATH"));
		PartdisBean.getInstance().setBackPath(this.getInitParameter("BACK_PATH"));
	}
	
	private void initThread(){
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
		try {
			scheduler.scheduleWithFixedDelay(getRunnable("com.linkage.ftpdrudgery.thread.PartFileAlarm"), 5l, Long.parseLong(this.getInitParameter("TIME_SLEEP")), TimeUnit.SECONDS);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private Runnable getRunnable(String s) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		return (Runnable) Class.forName(s).newInstance();
	}

}

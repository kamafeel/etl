package com.linkage.ftpdrudgery.tools;

/**
 * Fd内部异常类（未使用）
 * @author Run
 *
 */
public class FdException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3924415192523911163L;
	
	/* 异常错误码 */
	private int errorCode;
	
	
	public FdException(){
		
	}
	
	public FdException(String message){
		super(message);
	}
	
	public FdException(int errorCode, String message){
		super(message);
		this.errorCode = errorCode;
	}
	
	public FdException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public FdException(Throwable cause){
		super(cause);
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

}

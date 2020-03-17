package com.linkage.ftpdrudgery.fdenum;

/**
 * 
 * @author Run
 *
 */
public enum TaskStatusEnum {

	TaskStatus_Start("T1"),
	TaskStatus_GetFile_DownLoad("T2"),
	TaskStatus_GetFile_Transfer("T3"),
	TaskStatus_PutSourceFile_Upload("T41"),
	TaskStatus_PutSourceFile_Transfer("T51"),
	TaskStatus_PutReturnFile_Upload("T42"),
	TaskStatus_PutReturnFile_Transfer("T52"),
	TaskStatus_Plugin_ProduceFile("T6"),
	TaskStatus_Plugin_DisposeFile("T7"),
	TaskStatus_Complete("T8"),
	TaskStatus_NoFile("T9"),
	TaskStatus_Exception("T10");
	
	private String status;
	
	TaskStatusEnum(String status){
		this.status = status;
	}

	public String getStatus() {
		return status;
	}	
}

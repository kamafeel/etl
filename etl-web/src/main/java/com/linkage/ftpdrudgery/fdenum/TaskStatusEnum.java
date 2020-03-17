package com.linkage.ftpdrudgery.fdenum;

/**
 * 
 * @author Run
 *
 */
public enum TaskStatusEnum {

	T1("任务启动"),
	T2("获取文件_下载"),
	T3("获取文件_本地"),
	T41("原始文件_上传"),
	T51("原始文件_转移"),
	T42("回执文件_上传"),
	T52("回执文件_上传"),
	T6("插件_生成文件"),
	T7("插件_处理文件"),
	T8("任务完成"),
	T9("无文件"),
	T10("任务异常");
	
	private String status;
	
	TaskStatusEnum(String status){
		this.status = status;
	}

	public String getStatus() {
		return status;
	}	
}

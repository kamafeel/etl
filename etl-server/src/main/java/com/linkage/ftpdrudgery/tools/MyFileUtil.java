package com.linkage.ftpdrudgery.tools;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 文件处理类
 * @author run[zhangqi@lianchuang.com]
 * 3:39:17 PM May 26, 2009
 */

public class MyFileUtil {
	
	private static Logger logger = LoggerFactory.getLogger(MyFileUtil.class);
	
	private static String GlobalEncode = "GBK";
	
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;
	
	public static long freeMemory(){
		long size=(long)(Runtime.getRuntime().freeMemory()*(1.8f/3f)); 		
		if(size>0){
			return size;			 
		} else{
			return 1l; 
		}		
    }
	
	/**
	 * 拷贝文件
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException 
	 */
	public static void copyFile(String sourceFile, String destFile) throws IOException{
		MyFileUtil.copyFile(new File(sourceFile),new File(destFile));
	}
	
	/**
	 * 拷贝文件数组
	 * @param sourceFiles
	 * @param destDir
	 * @throws IOException 
	 */
	public static void copyFile(File[] sourceFiles, String destDir) throws IOException{
		
		for(File sourceFile : sourceFiles){
			File destFile = new File(destDir + File.separator + sourceFile.getName());
			if(destFile.isFile()){
				delFile(destFile);
			}
			copyFile(sourceFile, destFile);
		}
	}
	
	/**
	 * 拷贝文件数组
	 * @param sourceFiles
	 * @param destDir
	 * @throws IOException 
	 */
	public static void mvFile(File[] sourceFiles, String destDir) throws IOException{
		
		for(File sourceFile : sourceFiles){
			File destFile = new File(destDir + File.separator + sourceFile.getName());
			if(destFile.isFile()){
				delFile(destFile);
			}
			if(!sourceFile.getParentFile().isDirectory()){
				destFile.getParentFile().mkdirs();
			}
			sourceFile.renameTo(destFile);
		}
	}
	
	
	/**
	 * 拷贝文件(目标文件不存在,先建立)
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException 
	 */
	public static void copyFile(File sourceFile, File destFile) throws IOException {
				
		//目标文件不存在,先建立
		if(!destFile.isFile()){
			try {
				if(destFile.getParentFile() != null){
					if(!destFile.getParentFile().isDirectory()){
						destFile.getParentFile().mkdirs();
					}
				}
				destFile.createNewFile();
			} catch (IOException e) {
				throw e;
			}
		}
		try {
			MyFileUtil.CopyFileNIOStream(sourceFile,destFile);
		} catch (IOException e) {
			throw e;
		}		
//		if(sourceFile.length() < MyFileUtil.freeMemory()){
//			MyFileUtil.copyBigFile(sourceFile,destFile);
//        }else{
//        	copyFileNio(sourceFile, destFile);
//        }
	}
	
	/**
	 * 通道拷贝NIO
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException 
	 */
	public static void copyFileNio(File sourceFile, File destFile) throws IOException{
		FileChannel source = null;
		FileChannel destination = null;
		FileLock fl = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			fl = destination.tryLock();
			//增加独占锁定
			if(fl.isValid()){
				destination.transferFrom(source, 0, source.size());
			}else{
				logger.error("{}不能获取独占锁或共享锁,放弃写文件",sourceFile.getCanonicalPath());
			}
		} catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
        	try {
        		//释放锁
            	if(fl != null && fl.isValid()){
            		fl.release();
            	}
        		if (source != null) {
        			source.close();
        		}
    			if (destination != null) {
    				destination.close();
    			}
			} catch (IOException e) {
				throw e;
            }
		}
	}
	
	
	/**
	 * 拷贝大文件
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException 
	 */
	public static void copyBigFile(File sourceFile, File destFile) throws IOException{
        
        FileChannel source = null;
        FileChannel destination = null;
        ByteBuffer buffer = null;
        FileLock fl = null;
        try {
        	source = new FileInputStream(sourceFile).getChannel();
        	destination = new FileOutputStream(destFile).getChannel();
            
            /* 每次读取数据的缓存大小 */
        	//buffer = ByteBuffer.allocate((new Long((long)(MyFileUtil.freeMemory() * (2f/3f)))).intValue());
        	//buffer = ByteBuffer.allocateDirect((new Long((long)(MyFileUtil.freeMemory() * (2f/3f)))).intValue());        	
        	buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);       	
        	fl = destination.tryLock();
			//增加独占锁定
			if(fl.isValid()){
				while(source.read(buffer) != -1){
		                buffer.flip();
		                destination.write(buffer);
		                buffer.clear();
		        }
			}else{
				logger.error("{}不能获取独占锁或共享锁,放弃写文件",sourceFile.getCanonicalPath());
			}
			    
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }finally{
            // 关闭所有IO对象
            try {
            	//释放锁
            	if(fl != null && fl.isValid()){
            		fl.release();
            	}
                if(source!=null){
                	source.close();
                	source = null;
                }
                if(destination!=null){
                	destination.close();
                	destination = null;
                }
                if(buffer!=null){
                    buffer.clear();
                    buffer = null;
                }
            } catch (IOException e) {
                throw e;
            }
        }
    }
	
	/**
	 * 文件拷贝,NIO流
	 * @param input
	 * @param output
	 * @return
	 * @throws IOException
	 */
	public static long copyLarge(InputStream input, OutputStream output)
			throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		long count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}
	
	/**
	 * 文件拷贝,NIO流
	 * @param srcFile
	 * @param destFile
	 * @throws IOException
	 */
	public static void CopyFileNIOStream(File srcFile, File destFile) throws IOException{
		FileInputStream input = new FileInputStream(srcFile);
		try {
			FileOutputStream output = new FileOutputStream(destFile);			
			FileChannel destination = output.getChannel();
        	FileLock fl = destination.tryLock();
			try {
				if(fl.isValid()){
					MyFileUtil.copyLarge(input, output);
				}else{
					logger.error("{}不能获取独占锁或共享锁,放弃写文件",srcFile.getCanonicalPath());			
				}
			} finally {
				//释放锁
            	if(fl != null && fl.isValid()){
            		fl.release();
            	}
            	if(destination!=null){
                	destination.close();
                	destination = null;
                }           	
				if (output != null) {
					output.close();
				}				
			}
		} finally {
			if (input != null) {
				input.close();
			}
		}
	}
	
	
	/**
	 * 剪切文件
	 * 
	 * @param srcFile
	 * @param destFile
	 * @throws IOException 
	 */
	public static void cutFile(String sourceFile, String destFile) throws IOException {
		cutFile(new File(sourceFile), new File(destFile));
	}

	/**
	 * 剪切文件
	 * @param sourceFile
	 * @param destFile
	 * @throws IOException 
	 */
	public static void cutFile(File sourceFile, File destFile) throws IOException {
		copyFile(sourceFile, destFile);
		sourceFile.delete();
	}
		
	/**
	 * 建立目录
	 * @param fileDir
	 * @return
	 */
	public static boolean makeDirs(String fileDir) {		
		return new File(fileDir).mkdirs();
	}
		
	/**
	 * 批量剪切文件
	 * @param destDir
	 * @param sourceDir
	 * @param cutFiles
	 * @throws IOException 
	 */
	public static void cutFiles(File[] cutFiles, String destDir) throws IOException{
		
		for(File f : cutFiles){
			File destFile = new File(destDir + File.separator + f.getName());
			if(destFile.isFile()){
				delFile(destFile);
			}
			cutFile(f, destFile);
		}		
	}
	
	/**
	 * 清空文件夹中的所有文件(不递归文件夹)
	 * @param destDir
	 */
	public static void delFilesInDir(String destDir){
		delFilesInDir(new File(destDir));
	}
	
	
	/**
	 * 清空文件夹中的所有文件(不递归文件夹)
	 * @param destDir
	 */
	public static void delFilesInDir(File destDir){
		if(destDir.isDirectory()){
			File[] f = destDir.listFiles();
			for(File delf : f){
				delFile(delf);
			}
		}	
	}
		
	/**
	 * 批量删除
	 * @param destDir
	 * @param delNames
	 */
	public static void delFiles(File[] delFiles){		
		for(File f : delFiles){
			delFile(f);
		}
	}
	
	/**
	 * 删除文件(类似)(Z,gz)
	 * @param delFiles
	 * @param postfix
	 */
	public static void delFilesSim(File[] delFiles,String[] postfixs){		
		for(File f : delFiles){
			delFileSim(f,postfixs);
		}
	}
	
	/**
	 * 删除文件(类似)
	 * @param delFile
	 * @param postfix
	 */
	public static void delFileSim(File delFile,String[] postfixs){
		if(delFile.isFile()){
			delFile.delete();
		}else{
			logger.warn("{}不是文件或者它不存在,删除失败",delFile.getAbsolutePath());
			for(String postfix : postfixs){
				logger.warn("尝试删除:{}",delFile.getAbsolutePath().substring(0, delFile.getAbsolutePath().length()-postfix.length()));
				File f = new File(delFile.getAbsolutePath().substring(0, delFile.getAbsolutePath().length()-postfix.length()));
				if(f != null && f.isFile()){
					f.delete();
					break;
				}else{
					logger.warn("{}不是文件或者它不存在,删除失败",f.getAbsolutePath());
				}
			}			
		}
		
		//预处理文件删除
		File fPres = new File(delFile.getAbsolutePath() + ".pres");
		logger.warn("尝试删除:{}",fPres.getAbsolutePath());				
		if(fPres != null && fPres.isFile()){
			fPres.delete();
		}else{
			logger.warn("{}不是文件或者它不存在,删除失败",fPres.getAbsolutePath());
		}
		
	}
	
	/**
	 * 删除文件
	 * @param delFile
	 */	
	public static void delFile(File delFile){
		if(delFile.isFile()){
			delFile.delete();
		}else{
			logger.warn("{}不是文件或者它不存在,删除失败",delFile.getAbsolutePath());
		}
	}
		
	/**
	 * 拷贝文件夹(文件下所有文件)
	 * @param srcDirectory
	 * @param destDirectory
	 * @throws IOException 
	 */
	public static void copyDir(String srcDir, String destDir) throws IOException{    
        copyDir(new File(srcDir),new File(destDir));
    }
    
	/**
	 * 拷贝文件夹(文件下所有文件)
	 * @param srcDir
	 * @param destDir
	 * @throws IOException 
	 */
    public static void copyDir(File srcDir, File destDir) throws IOException{        
        
        /* 得到目录下的文件和目录数组 */
        File[] fileList = srcDir.listFiles();      
        for (File srcf : fileList) {
            if (srcf.isFile()) {
                if (!destDir.exists()) {
                	destDir.mkdirs();
                }
                File destf = new File(destDir.getAbsolutePath() + File.separatorChar + srcf.getName());
                //如果目标文件夹存在此文件,先删除
                delFile(destf);
                //拷贝文件
                copyFile(srcf,destf);  
            } else {
            	//数组中的对象为目录,如果该子目录在目标文件夹中不存在就创建
                File subDir = new File(destDir.getAbsolutePath() + File.separatorChar + srcf.getName());
                if (!subDir.exists()) {
                    subDir.mkdirs();
                }
                //递归调用自己
                copyDir(srcf,subDir);
            }
        }
        fileList = null;
    }
    
    /**
     * 剪切目录
     * @param srcDir
     * @param destDir
     * @throws IOException 
     */
    public static void cutDir(String srcDir, String destDir) throws IOException{
    	cutDir(new File(srcDir),new File(destDir));
    }
    
    /**
     * 剪切目录
     * @param srcDir
     * @param destDir
     * @throws IOException 
     */
    public static void cutDir(File srcDir, File destDir) throws IOException{
        copyDir(srcDir,destDir);
        srcDir.delete();
    }
	
	/**
	 * 写内容到文件中
	 * @param file
	 * @param fileContent
	 * @param tag
	 * @return
	 */
	public static boolean StringToFile(File file, String fileContent, boolean tag) {
		FileWriter fileWriter = null;
		try {
			if (!new File(file.getParent()).exists()) {
				new File(file.getParent()).mkdirs();
				logger.warn("目录{}不存在,程序建立目录",file.getAbsolutePath());
				//file.createNewFile();
			}
			fileWriter = new FileWriter(file, tag);
			fileWriter.write(fileContent);
			fileWriter.flush();
			fileWriter.close();
			//logger.debug("记录文件{}开始记录操作成功的文件名:{}",file.getAbsolutePath(),fileContent);
		} catch (IOException e) {
			logger.error("记录操作成功文件异常", e);
			return false;
		} finally{
			if(fileWriter != null){
				try {
					fileWriter.close();
				} catch (IOException e) {
					logger.error("关闭FileWriter异常", e);
				}
			}
		}
		return true;
	}
	
	/**
	 * 读取文件
	 * @param destFile
	 * @return
	 * @throws Exception 
	 * @throws IOException 
	 */
	public static String readFile(String destFile) throws IOException, Exception{
		return decodeByteBuffer(readFileToByteBuffer(new File(destFile)), GlobalEncode);
	}
	
	/**
	 * 读取文件
	 * @param destFile
	 * @return
	 * @throws Exception 
	 * @throws IOException 
	 */
	public static String readFile(File destFile) throws IOException, Exception{
		return decodeByteBuffer(readFileToByteBuffer(destFile), GlobalEncode);
	}
	
	/**
	 * 读取文件
	 * @param destDir
	 * @return
	 * @throws Exception 
	 * @throws IOException 
	 */
	public static String readFile(File destFile, String encode) throws IOException, Exception{	
		return decodeByteBuffer(readFileToByteBuffer(destFile), encode);		
	}
	
	/**
	 * 获取文件夹下面所有文件(递归查找)
	 * @param srcDir
	 * @return
	 */
	public static File[] getFiles(String srcDir){
		File f = new File(srcDir);
		if(f.isDirectory()){
			return getFiles(f);
		}else{
			logger.error("递归查找根目录{}不是一个文件夹,递归查找文件失败",srcDir);
			return null;
		}
	}
	
	/**
	 * 获取文件夹下面所有文件(递归查找)
	 * @param srcDir
	 * @return
	 */
	public static File[] getFiles(File srcDir){
		ArrayList<File> fl = getFiles(srcDir, null);
		File[] files = new File[fl.size()];
		int num = 0;
		for(File f : fl){
			files[num] = f;
			num++;
		}
		return files;
	}
	
	/**
	 * 获取文件夹下面所有文件(递归查找)
	 * @param srcDir
	 * @param f
	 * @return
	 */
	public static ArrayList<File> getFiles(File srcDir, ArrayList<File> f){
		ArrayList<File> fl = null;
		if (f == null) {
			fl = new ArrayList<File>();
		} else {
			fl = new ArrayList<File>(f);
		}
		/* 得到目录下的文件和目录数组 */
		File[] fileList = srcDir.listFiles();		
		for (File srcf : fileList) {
			if (srcf.isFile()) {
				fl.add(srcf);
			} else {
				// 递归调用,并赋值给fl
				fl = getFiles(srcf, fl);
			}
		}
        return fl;
	}
	
	
	/**
	 * 获取文件夹下面所有文件(递归查找)
	 * @param srcDir
	 * @return
	 */
	public static File[] getFilesFilter(String srcDir,FilenameFilter ff){
		File f = new File(srcDir);
		if(f.isDirectory()){
			return getFilesFilter(f,ff);
		}else{
			logger.error("递归查找根目录{}不是一个文件夹,递归查找文件失败",srcDir);
			return null;
		}
	}
	
	/**
	 * 获取文件夹下面所有文件(递归查找)
	 * @param srcDir
	 * @return
	 */
	public static File[] getFilesFilter(File srcDir,FilenameFilter ff){
		ArrayList<File> fl = getFilesFilter(srcDir, null,ff);
		File[] files = new File[fl.size()];
		int num = 0;
		for(File f : fl){
			files[num] = f;
			num++;
		}
		return files;
	}
	
	/**
	 * 获取文件夹下面所有文件(递归查找)
	 * @param srcDir
	 * @param f
	 * @return
	 */
	public static ArrayList<File> getFilesFilter(File srcDir, ArrayList<File> f,FilenameFilter ff){
		ArrayList<File> fl = null;
		if (f == null) {
			fl = new ArrayList<File>();
		} else {
			fl = new ArrayList<File>(f);
		}
		/* 得到目录下的文件和目录数组 */
		File[] fileList = srcDir.listFiles(ff);
		for (File srcf : fileList) {
			if (srcf.isFile()) {
				fl.add(srcf);
			} else {
				// 递归调用,并赋值给fl
				fl = getFiles(srcf, fl);
			}
		}
        return fl;
	}
	
	/**
	 * 编码ByteBuffer
	 * 
	 * @param buffer
	 * @param encode
	 * @return
	 * @throws Exception 
	 */
	public static String decodeByteBuffer(ByteBuffer buffer, String encode) throws Exception{
		Charset charset = null;
		CharsetDecoder decoder = null;
		CharBuffer charBuffer = null;
		try {
			charset = Charset.forName(encode);
			decoder = charset.newDecoder();
			charBuffer = decoder.decode(buffer);
			return charBuffer.toString();
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 读取文件(ByteBuffer)
	 * @param destFile
	 * @return
	 * @throws IOException 
	 */
	public static ByteBuffer readFileToByteBuffer(File destFile) throws IOException{	
		FileChannel source = null;
		MappedByteBuffer mBuf = null;
		try {
			source = new FileInputStream(destFile).getChannel();		
			mBuf = source.map(FileChannel.MapMode.READ_ONLY, 0, source.size());
		} catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
        	try {
        		if (source != null) {
				source.close();
        		}
			} catch (IOException e) {
				throw e;
            } 			
			source = null;
		}
        return mBuf;
	}
	
	/**
	 * 读取String按照给定的Delimiter
	 * @param destFile
	 * @param delimiter
	 * @return
	 * @throws Exception 
	 * @throws IOException 
	 */
	public static String[] readFileByDelimiter(File destFile, String delimiter) throws IOException, Exception{
		String content = readFile(destFile);
		if(CommonTool.checkNull(content)){
			return null;
		}
		return content.split(delimiter);
	}
	
	/**
	 * 读取String按照给定的Delimiter
	 * @param destFiles
	 * @param delimiter
	 * @return
	 * @throws Exception 
	 * @throws IOException 
	 */
	public static String[] readFilesByDelimiter(File[] destFiles, String delimiter) throws IOException, Exception{	
		String content = "";	
		for(File f : destFiles){
			content += readFile(f);
		}
		if(CommonTool.checkNull(content)){
			return null;
		}
		return content.split(delimiter);
	}
	
	/**
	 * 读取String按照给定的Delimiter(返回ArrayList<String>)
	 * @param destFiles
	 * @param delimiter
	 * @return
	 * @throws Exception 
	 * @throws IOException 
	 */
	public static ArrayList<String> readFilesByDelimiterToArrayList(File[] destFiles, String delimiter) throws IOException, Exception{
		ArrayList<String> arrayList = new ArrayList<String>();
		
		String content = "";	
		for(File f : destFiles){
			if(f.isFile()){
				content += readFile(f);
			}else{
				logger.warn("{}记录文件不存在,程序忽略",f.getName());
			}
			
		}
		if(CommonTool.checkNull(content)){
			return arrayList;
		}
		String[] split = content.split(delimiter);
			
		for(String s : split){
			arrayList.add(s);
		}
		return arrayList;
	}
	
	
	/**
	 * 单元测试代码
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
	}
}

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.linkage.intf.file.FileUtil;


public class AnsylFile {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//1G
		long fileMaxSize = 1024*1024*1024;
		//System.out.println(fileMaxSize);
		// TODO Auto-generated method stub
		File[] a = FileUtil.getFiles("D:\\test\\");
		List<File> ll = new ArrayList<File>();
		for(File f : a){
			if(f.length() < fileMaxSize){
				System.out.println(f.getName());
				
				Pattern p = Pattern.compile("\\d+");
				Matcher m  = p.matcher(f.getName());
				List<Long> LoadLogNum = new ArrayList<Long>();
				while (m.find()){
					LoadLogNum.add(Long.parseLong(m.group(0).toString()));
					System.out.println(m.group(0).toString());
				}
				
			}
		}
		
	}

}

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.linkage.intf.tools.DESPlus;
import com.linkage.intf.tools.RandomUtils;


public class ReadMa {
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		String a = "INSERT INTO VGOP_ODS.FD_PLUGIN_INFO (PLUGIN_ID, FILE_DATE, LOAD_STR, PER_SHELL, PLUGIN_INFO) VALUES (";
		Properties p = new Properties();
		InputStream in = new BufferedInputStream(new FileInputStream("C:\\Users\\Run\\Desktop\\20121113\\20121113\\plugin\\ods_load\\bus\\filedate.properties"));		
		p.load(in);
		HashMap<String,String> sss = new HashMap<String,String>();
		for(Enumeration e = p.propertyNames();e.hasMoreElements();){
			String key = (String)e.nextElement();
			sss.put(key, a + "'"  +  20121113 + RandomUtils.randomNumeric(5) + "'," + p.getProperty(key));
			
		}
		p.clear();
		
		
		
		in = new BufferedInputStream(new FileInputStream("C:\\Users\\Run\\Desktop\\20121113\\20121113\\plugin\\ods_load\\bus\\preshell.properties"));
		p.load(in);
		
		for(Enumeration e = p.propertyNames();e.hasMoreElements();){
			String key = (String)e.nextElement();
			if(sss.containsKey(key)){
				sss.put(key, sss.get(key) + ",'" + new DESPlus("Test").encrypt(p.getProperty(key)) + "'");
			}else{
				sss.put(key, a + "'"  +  20121113 + RandomUtils.randomNumeric(5) + "',0,'" + new DESPlus("Test").encrypt(p.getProperty(key)) + "'");
			}
			
		}
		p.clear();

		
		in = new BufferedInputStream(new FileInputStream("C:\\Users\\Run\\Desktop\\20121113\\20121113\\plugin\\ods_load\\bus\\loadPara.properties"));		
		p.load(in);
		for(Enumeration e = p.propertyNames();e.hasMoreElements();){
			String key = (String)e.nextElement();
			if(sss.containsKey(key)){
				sss.put(key, sss.get(key) + ",'" + new DESPlus("Test").encrypt(p.getProperty(key)) + "'");
			}
		}
		p.clear();

		
		in = new BufferedInputStream(new FileInputStream("C:\\Users\\Run\\Desktop\\20121113\\20121113\\plugin\\ods_load\\bus\\replace.properties"));		
		p.load(in);
		for(Enumeration e = p.propertyNames();e.hasMoreElements();){
			String key = (String)e.nextElement();
			if(sss.containsKey(key)){
				sss.put(key, sss.get(key) + ",'" + new DESPlus("Test").encrypt(p.getProperty(key)) + "');");
			}
		}
		p.clear();
		
		
		for (Iterator it = sss.entrySet().iterator(); it.hasNext();) {
			Map.Entry e = (Map.Entry) it.next();
			System.out.println((String)e.getValue());
		}
	}

}

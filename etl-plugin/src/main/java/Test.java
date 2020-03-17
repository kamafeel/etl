

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.linkage.ftpdrudgery.db.ods.bean.OdsPubLog;
import com.linkage.intf.file.FileUtil;
import com.linkage.intf.file.ZipFileUtil;
import com.linkage.intf.tools.StringUtils;
import com.linkage.intf.tools.TimeUtils;

public class Test {
	
	/**
	 * 解压缩ZIP
	 * @param f
	 * @return
	 * @throws IOException
	 */
	private File[] UnZip(File f) throws IOException{
		ZipFileUtil.getInstance().unZip(f.getAbsolutePath().substring(0, f.getAbsolutePath().length()-".zip".length()),f.getAbsolutePath());		
		return new File(f.getParent()).listFiles();		
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
//		String a ="222,11";
//		System.out.println(a.split("\\,")[0]);
		
//		String a = "20110910=20110922042351=a=00=gg=40=0=460008228051814=BELTB=13908202990=20110910075639=26488=D5B53D6E==0517==0FF1=1edd4103=DDB100F2=C"+
//"MNET==1=0A03AEEF=0=0=S=001=A=51552=184573=26488=B=0=0=0=C=0=0=0=D=0=0=0=E=0=0=0=F=0=0=0=0032=0=6=0=4.62=0.00=0.00=0.00=4.62=0.00=0.0"+
//"0=0.00=r20110922041500.8438.BILL13=110922040334=I=00000=0dn00=0.00=0.00=00000Y=5016$$00=zzzzzzzzzz==wggsnIG20110922280.001.1331.dp";
//		
//		
//		System.out.println(a.split("\\=").length);
		
		
		//System.out.println("222\n222");
		
		
//		StringBuilder sb = new StringBuilder();
//		sb.append("234");
//		sb.delete(2, 3);
//		
//		System.out.println(sb.toString());
		
		
		//String a = FileUtil.readFile("D:\\编辑3.TXT");
		
		//System.out.println(a);
		
		//int ww = StringUtils.countMatches(a, "+00000000");
		//System.out.println(ww);
		
//		String a= "Rows Read                   = 85";
//		Object[] os = new MessageFormat("Rows Read                   = {0,number,integer}").parse(a);
//		for(Object o :os){
//			System.out.println(o);
//		}
		
//		System.out.println(a.indexOf("Rows Read"));
//		System.out.println(a.indexOf("Rows Rejected"));
//		String b = a.substring(a.indexOf("Rows Read"), a.indexOf("Rows Rejected"));
//		System.out.println(b);
//		
//		
//		Object[] os = new MessageFormat("Rows Read                   = {0,number,integer}").parse(b);
//		for(Object o :os){
//			System.out.println(o);
//		}
		
		
//		String a="load client from /interface/ftp/bi/gprs/gprs.2011100909.20111008.out.9459 of del modified by codepage=1386 dateformat=\"YYYYMMDD\" timestampformat=\"YYYYMMDDHHMMSS\" coldel= norowwarnings messages /interface/ods_232/load_log/gprs.2011100909.20111008.out.9459.Z.log insert into VGOP_ODS.ODS_NG_GPRS_DTL_DS_20111008 nonrecoverable";
//		System.out.println(a);
		
//		String a ="load client from 数据文件路径 of del modified by codepage=1386 coldel= norowwarnings messages [日志文件路径] insert into [表名] nonrecoverable";
//		a = a.replaceFirst("数据文件路径", "interface/ftp/bi/gprs/gprs.2011100909.20111008.out.9459");
//		System.out.println(a);
		
//		String a ="0936020110926dcolorring.c.avl.gz";
//		System.out.println(a.substring(5, "yyyyMMdd".length()+5));
		
		
//		String a = "5,yyyyMMdd";
//		System.out.println(a.split("\\,").length);
		
		//System.out.println(FileUtil.readFile("d:\\2.s"));
	
//		String a = "20111008,,13882310988   ,,d,a ,0401001,5,00026a00,神州行阳光卡                  ,,,001,炳草岗营业厅,00,-1,C4,,,,,A,20100724,20021202"+
//",a001$$00,GPRS自由套餐,35200004337417,1,0,00,,,,,,,,,,,,0,,,,,,00,,诺基亚,5233,1,1,0,1,0,0,,,,,,,gn,69064";
//		
//		String[] aa = a.split("\\,");
//		
//		for(String s : aa){
//			System.out.println(s);
//		}
		
		//System.out.println("walkman_baoyue_sichuan_20111012_679_day.txt".matches("(?:walkman_baoyue_sichuan_)\\d{8}[_]{1}\\d{3,9}(?:_day)[.]{1}(?:txt)"));
//		String a ="walkman_baoyue_sichuan_20111012_679_day.txt";
//		System.out.println(a.substring(23, "yyyyMMdd".length()+23));
		
//		new File("d:\\1.txt").createNewFile();
		//System.out.println("cailing_sichuan_201108_535667_month.txt".matches("(?:cailing_sichuan_)\\d{8}[_]{1}\\d{3,9}(?:_month)[.]{1}(?:txt)"));
//		String a = "21&1088072&汶川县耿达乡方强代办（空中营业厅）&15983701441    &ubcb01&20080922173533&0&1&uaqq04&20080922173533&800260907463& &汶川耿达乡方强代办&1047870& & &1&ebkebk  &0&1088072& ";
//		String[] aa = a.split("\\&");
//		for(String s : aa){
//			System.out.println(s);
//		}
		
		
		//System.out.println("mms_hw_server52_20111019.txt".matches("((?:hd_)\\d{8}(?:_hw_all.txt)|(?:mms_hd_)\\d{8}(?:.txt)|(?:mms_hw_server)\\d{2}[_]{1}\\d{8}(?:.txt))"));
		
//		String a = "aaaa";
//		String b = "";
//		System.out.println(a+b);
		
		//System.out.println(new File("d:\\新建文本文档.txt").length());
		
		//System.out.println(new File("D:\\sp\\back\\sp\\excel\\boce\\Orig\\20110831\\").getAbsolutePath());
		//System.out.println("张琦20110801".substring(2, 10));
		
		//System.out.println(new Test().UnZip(new File("D:\\temp\\sc_huadan_20111025.zip")));;
		//FileUtil.delDir("D:\\temp\\sc_huadan_20111025");
		
		
		//System.out.println(TimeUtils.string2Date("2009年03月", "yyyy年dd月"));
		
		
//		String a = "  RESULTS:       8 of 8 LOADs completed successfully.\n"+
//			"______________________________________________________________________________\n"+
//			"Summary of Partitioning Agents:\n"+
//"Rows Read                   = 18104446\n"+
//"Rows Rejected               = 0\n"+
//"Rows Partitioned            = 18104446\n"+
//
//"Summary of LOAD Agents:\n"+
//"Number of rows read         = 18104446\n"+
//"Number of rows skipped      = 0\n"+
//"Number of rows loaded       = 18104332\n"+
//"Number of rows rejected     = 114\n"+
//"Number of rows deleted      = 0\n"+
//"Number of rows committed    = 18104446\n"+
//"SQL3107W  There is at least one warning message in the message file.\n"+
//"DB20000I  The TERMINATE command completed successfully\n";
//	
//		//System.out.println(a);
//		
//		//System.out.println(a.indexOf("Summary of Partitioning Agents"));
//		//System.out.println(a.indexOf("The TERMINATE command completed successfully"));
//		
//		//System.out.println(a.substring(a.indexOf("Summary of Partitioning Agents"), a.indexOf("DB20000I  The TERMINATE command completed successfully")));
//		Pattern p = Pattern.compile("(\\d+)");
//		Matcher m  = p.matcher(a);
//		
//		while (m.find()){
//			System.out.println(m.group(1).toString());
//			
//		}
		
		//System.out.println(new OdsPubLog().getOdsFlag());
//		Calendar c = Calendar.getInstance();
//		c.setTime(TimeUtils.string2Date("2011-11-10", "yyyy-MM-dd"));
//		c.add(Calendar.DAY_OF_MONTH, 7);
//		System.out.println(c.getTime());
		//if(){
			
		//}
		//System.out.println(TimeUtils.date2String(c.getTime(), "yyyy-MM-dd HH:mm:ss"));
		
//		File a = new File("D:\\temp\\p_FtpDrudgery.rar");
//		System.out.println(a.getAbsolutePath());
		
		
		//System.out.println(TimeUtils.date2String(new Date(), "yyyyMM"));
		//System.out.println("yyyy-MM-dd".contains("dd"));
		
		System.out.println("SQL0964C  The transaction log for the database is full.  SQLSTATE=57011".contains("SQLSTATE"));
	}

}

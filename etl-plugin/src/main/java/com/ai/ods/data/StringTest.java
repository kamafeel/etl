package com.ai.ods.data;

import com.linkage.intf.tools.TimeUtils;

public class StringTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String a = "VGOP_DWD.DWDW_SSS_SS_MS_��������";
		a =a.replaceFirst("��������","201304");
		
		System.out.println(a);
	}

}

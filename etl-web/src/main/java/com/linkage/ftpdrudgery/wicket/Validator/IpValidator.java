package com.linkage.ftpdrudgery.wicket.Validator;

import java.util.regex.Pattern;

import org.apache.wicket.validation.validator.PatternValidator;

/**
 * IP校验
 * @author run[zhangqi@lianchuang.com]
 * 9:43:02 PM May 21, 2009
 */
public class IpValidator extends PatternValidator {

	private static final long serialVersionUID = 1L;

	private static final IpValidator INSTANCE = new IpValidator();

	public static IpValidator getInstance()
	{
		return INSTANCE;
	}

	protected IpValidator()
	{	
		//^(1?\\d{1,2}|2[0-4]\\d|25[0-5])(\\.1?\\d{1,2}|2[0-4]\\d|25[0-5]){3}$
		super(
			"\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}",
			Pattern.CASE_INSENSITIVE);
	}

}

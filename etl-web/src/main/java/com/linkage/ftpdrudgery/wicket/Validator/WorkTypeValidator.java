package com.linkage.ftpdrudgery.wicket.Validator;

import java.util.regex.Pattern;

import org.apache.wicket.validation.validator.PatternValidator;

/**
 * IP校验
 * @author run[zhangqi@lianchuang.com]
 * 9:43:02 PM May 21, 2009
 */
public class WorkTypeValidator extends PatternValidator {

	private static final long serialVersionUID = 1L;

	private static final WorkTypeValidator INSTANCE = new WorkTypeValidator();

	public static WorkTypeValidator getInstance()
	{
		return INSTANCE;
	}

	protected WorkTypeValidator()
	{
		super(
			"(?:(?:PORT)|(?:PASV))",
			Pattern.CASE_INSENSITIVE);
	}

}

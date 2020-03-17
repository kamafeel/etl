package com.linkage.ftpdrudgery.wicket.Validator;

import java.util.regex.Pattern;

import org.apache.wicket.validation.validator.PatternValidator;

/**
 * IP校验
 * @author run[zhangqi@lianchuang.com]
 * 9:43:02 PM May 21, 2009
 */
public class TransfersTypeValidator extends PatternValidator {

	private static final long serialVersionUID = 1L;

	private static final TransfersTypeValidator INSTANCE = new TransfersTypeValidator();

	public static TransfersTypeValidator getInstance()
	{
		return INSTANCE;
	}

	protected TransfersTypeValidator()
	{
		super(
			"(?:(?:BINARY)|(?:ASCII))",
			Pattern.CASE_INSENSITIVE);
	}

}

package com.smart.helper;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<isValidPassword, String> {

	@Override
	public void initialize(isValidPassword constraintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if(value.contains("@")||value.contains("#")||value.contains("$")||value.contains("%")||value.contains("&")) {
			return true;
		}
		return false;
	}
	
}

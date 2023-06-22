package com.smart.entities;

import com.smart.helper.isValidPassword;

public class ChangePassword {

	private String oldPassword;
	@isValidPassword
	private String newPassword;
	
	private String conPassword;

	public ChangePassword(String oldPassword, String newPassword, String conPassword) {
		super();
		this.oldPassword = oldPassword;
		this.newPassword = newPassword;
		this.conPassword = conPassword;
	}
	

	public ChangePassword() {
		super();
		// TODO Auto-generated constructor stub
	}


	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getConPassword() {
		return conPassword;
	}

	public void setConPassword(String conPassword) {
		this.conPassword = conPassword;
	}
	
	
	
}

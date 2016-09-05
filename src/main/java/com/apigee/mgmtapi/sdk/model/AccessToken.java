package com.apigee.mgmtapi.sdk.model;

public class AccessToken {

	private String access_token;
	private String token_type;
	private String refresh_token;
	
	public String getAccess_token() {
		return access_token;
	}
	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}
	public String getToken_type() {
		return token_type;
	}
	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}
	public String getRefresh_token() {
		return refresh_token;
	}
	public void setRefresh_token(String refresh_token) {
		this.refresh_token = refresh_token;
	}
	
	@Override
	public String toString() {
		return "AccessToken [access_token=" + access_token + ", token_type=" + token_type + ", refresh_token="
				+ refresh_token + "]";
	}
}

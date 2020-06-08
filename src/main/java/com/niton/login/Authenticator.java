package com.niton.login;

public interface Authenticator<PT> {
	public boolean authenticate(String user,PT password);
}

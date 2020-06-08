package com.niton.login;

public enum LoginResult {
	VERIFIED(true),
	ON_COOLDOWN(false),
	WAITING_AREA(false),
	UNSUCCESSFULL(false),
	BLOCKED(false);
	public final boolean success;

	LoginResult(boolean success) {
		this.success = success;
	}
}

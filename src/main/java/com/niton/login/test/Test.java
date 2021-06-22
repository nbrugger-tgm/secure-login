package com.niton.login.test;

import com.niton.login.*;

public class Test {
	public static void main(String[] args) {
		LoginHandler<String> h = new LoginHandler<>(new Authenticator<String>() {
			@Override
			public boolean authenticate(String user, String password) {
				return false;
			}
		});
		h.setListener(new LoginListener() {
			@Override
			public void banIP(String ip, BanReason reason) {
				System.out.println("Test.banIP");
			}

			@Override
			public void ipAccessAccountLogin(String ip, String account) {
				System.out.println("Test.ipAccessAccountLogin");
			}

			@Override
			public void loginFail(String ip, String account) {
				System.out.println("Test.loginFail");
			}

			@Override
			public void guessInCooldown(String ip, String account) {
				System.out.println("Test.guessInCooldown");
			}

			@Override
			public void loginSuccess(String ip, String account) {
				System.out.println("Test.loginSuccess");
			}

			@Override
			public void loginTry(String ip, String account) {
				System.out.println("Test.loginTry");
			}

			@Override
			public void exceedBasicTries(String ip, String account) {
				System.out.println("Test.exceedBasicTries");
			}

			@Override
			public void guessInWaitingArea(String ip, String account) {
				System.out.println("Test.guessInWaitingArea");
			}

			@Override
			public void alertAccount(String account, String ip, AccountAlertReason reason) {
				System.out.println("Test.alertAccount");
			}

			@Override
			public void bannedIPRequest(String ip) {
				System.out.println("Test.bannedIPRequest");
			}
		});




		String acc = "root";
		for(int i = 0;i<200;i++){
			h.handle(acc, "", "localhost");
		}
	}
}

package com.niton.login;

public interface LoginListener {
	void banIP(String ip, BanReason reason);
	void ipAccessAccountLogin(String ip, String account);
	void loginFail(String ip, String account);
	void guessInCooldown(String ip,String account);
	void loginSuccess(String ip,String account);
	void loginTry(String ip,String account);
	void exceedBasicTries(String ip,String account);
	void guessInWaitingArea(String ip,String account);
	void alertAccount(String account,AccountAlertReason reason);
	void bannedIPRequest(String ip);
	abstract class AlertListener implements LoginListener{

		@Override
		public void ipAccessAccountLogin(String ip, String account) {

		}

		@Override
		public void loginFail(String ip, String account) {

		}


		@Override
		public void loginSuccess(String ip, String account) {

		}

		@Override
		public void loginTry(String ip, String account) {

		}

		@Override
		public void exceedBasicTries(String ip, String account) {

		}

		@Override
		public void guessInWaitingArea(String ip, String account) {

		}

		@Override
		public void bannedIPRequest(String ip) {

		}
	}
}

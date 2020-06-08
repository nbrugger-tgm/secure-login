package com.niton.login;

public interface LoginListener {
	public void banIP(String ip,BanReason reason);
	public void ipAccessAccountLogin(String ip,String account);
	public void loginFail(String ip,String account);
	public void guessInCooldown(String ip,String account);
	public void loginSuccess(String ip,String account);
	public void loginTry(String ip,String account);
	public void exceedBasicTries(String ip,String account);
	public void guessInWaitingArea(String ip,String account);
	public void alertAccount(String account,AccountAlertReason reason);
	public void bannedIPRequest(String ip);
	public static abstract class AlertListener implements LoginListener{

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

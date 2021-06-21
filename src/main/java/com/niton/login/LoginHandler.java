package com.niton.login;

import com.niton.login.cfg.LoginSecurityConfig;
import com.typesafe.config.ConfigFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LoginHandler<PT> implements LoginListener {
	// Account related
	private final HashMap<String, Integer> accountTries = new HashMap<>();
	private final Set<String>              warned       = new HashSet<>();
	private final HashMap<String, Set<String>> waitingArea         = new HashMap<>();
	private final HashMap<String, Long>        accLastRequest      = new HashMap<>();
	private final HashMap<String, Set<String>> accountAccessingIPs = new HashMap<>();

	//IP related
	private final HashMap<String, Integer> ipTries = new HashMap<>();
	private final Set<String>              blocked = new HashSet<>();
	private final HashMap<String, Long>                     banEnteringDates     = new HashMap<>();
	private final HashMap<String, Long>                     ipLastRequest        = new HashMap<>();
	private final HashMap<String, HashMap<String, Long>>    waitingAreaEnterTime = new HashMap<>();
	private final HashMap<String, HashMap<String, Integer>> legidGuessCounter    = new HashMap<>();
	/**
	 * Stores the account each ip accessed {@code <ip, accessedAccounts>}
	 */
	private final HashMap<String, Set<String>>              ipsAccountAccessing  = new HashMap<>();

	private LoginSecurityConfig config;
	private Authenticator<PT> auther;

	public LoginListener getListener() {
		return listener;
	}

	public void setListener(LoginListener listener) {
		this.listener = listener;
	}

	private LoginListener listener = new AlertListener() {
		@Override
		public void banIP(String ip, BanReason reason) {
		}

		@Override
		public void guessInCooldown(String ip, String account) {
		}

		@Override
		public void alertAccount(String account, String ip, AccountAlertReason reason) {
		}
	};

	private LoginResult res;

	public LoginHandler(LoginSecurityConfig config, Authenticator<PT> auther) {
		this.config = config;
		this.auther = auther;
	}

	public LoginHandler(Authenticator<PT> auther) {
		this(new LoginSecurityConfig(ConfigFactory.load()), auther);
	}

	public LoginResult handle(String account, PT password, String ip) {
		res = null;
		loginTry(ip, account);
		if (res != null)
			return res;

		long waitingAreaMS = Duration.ofMinutes(config.security.login.waiting_area_time).toMillis();
		long waitinAreaEnterTime = waitingAreaEnterTime.getOrDefault(account, new HashMap<>()).getOrDefault(ip, 0L);
		if (Timing.isOver(waitinAreaEnterTime, waitingAreaMS)) {
			waitingArea.getOrDefault(account, new HashSet<>()).remove(ip);
		} else {
			guessInWaitingArea(ip, account);
			return res;
		}

		long lastIPRequest = ipLastRequest.getOrDefault(ip,0L);
		long lastAccRequest = accLastRequest.getOrDefault(account,0L);
		long delayMS = (long) (config.security.login.login_delay * 1000);
		boolean isIpEligibleForRequest = Timing.isOver(lastIPRequest, delayMS);
		boolean isAccEligibleForRequest = Timing.isOver(lastAccRequest, delayMS);

		if (!isIpEligibleForRequest || !isAccEligibleForRequest) {
			guessInCooldown(ip, account);
			return res;
		}

		HashMap<String, Integer> thisAccountLegidCount = legidGuessCounter.getOrDefault(account, new HashMap<>());
		thisAccountLegidCount.put(ip, thisAccountLegidCount.getOrDefault(ip, 0) + 1);
		legidGuessCounter.put(account, thisAccountLegidCount);

		ipAccessAccountLogin(ip, account);
		if (res != null)
			return res;

		//messing with database by timing determinition
		try {
			Thread.sleep((long) (Math.random() * 100));
		} catch (InterruptedException e) {
		}
		boolean valid = auther.authenticate(account, password);
		if (valid) {
			ipsAccountAccessing.getOrDefault(ip, new HashSet<>()).remove(account);
			accountAccessingIPs.getOrDefault(account, new HashSet<>()).remove(ip);
			this.legidGuessCounter.getOrDefault(account, new HashMap<>()).remove(ip);
			this.warned.remove(account);
			this.waitingArea.getOrDefault(account, new HashSet<>()).remove(ip);
			this.ipTries.remove(ip);
			loginSuccess(ip, account);
			return LoginResult.VERIFIED;
		} else {
			loginFail(ip, account);
			if (thisAccountLegidCount.getOrDefault(ip, 0) > config.security.login.tries_bevore_waiting)
				exceedBasicTries(ip, account);
			return LoginResult.UNSUCCESSFULL;
		}
	}

	@Override
	public void banIP(String ip, BanReason reason) {
		listener.banIP(ip, reason);
		for (String account : ipsAccountAccessing.getOrDefault(ip, new HashSet<>())) {
			alertAccount(account, ip, AccountAlertReason.BANNED_IP_GUESSED);
		}
		blocked.add(ip);
		banEnteringDates.put(ip, System.currentTimeMillis());
		ipsAccountAccessing.remove(ip);
		ipTries.remove(ip);
		ipLastRequest.remove(ip);
		res = LoginResult.BLOCKED;
	}

	@Override
	public void ipAccessAccountLogin(String ip, String account) {
		listener.ipAccessAccountLogin(ip, account);
		Set<String> ipsForThisAccount = accountAccessingIPs.getOrDefault(account, new HashSet<>());
		ipsForThisAccount.add(ip);
		accountAccessingIPs.put(account, ipsForThisAccount);

		Set<String> accountsForThisIp = ipsAccountAccessing.getOrDefault(ip, new HashSet<>());
		accountsForThisIp.add(account);
		ipsAccountAccessing.put(account, accountsForThisIp);


		if (accountsForThisIp.size() > config.security.login.max_accounts_per_ip)
			banIP(ip, BanReason.ACCESSED_TO_MANY_ACCOUNTS);

		if (accountAccessingIPs.size() > config.security.login.max_ips_per_account)
			alertAccount(account, ip, AccountAlertReason.TO_MANY_IPS_ACCESSING);
	}

	@Override
	public void loginFail(String ip, String account) {
		listener.loginFail(ip, account);

		ipLastRequest.put(ip, System.currentTimeMillis());
		accLastRequest.put(account, System.currentTimeMillis());
	}

	@Override
	public void guessInCooldown(String ip, String account) {
		listener.guessInCooldown(ip, account);
		if (warned.contains(account))
			banIP(ip, BanReason.GUESS_ON_WARNED_ACCOUNT);
		ipLastRequest.put(ip, System.currentTimeMillis());
		legidGuessCounter.getOrDefault(account, new HashMap<>()).put(ip,legidGuessCounter.getOrDefault(account, new HashMap<>()).getOrDefault(ip,0 )+1);
		accLastRequest.put(account, System.currentTimeMillis());
		if (res == null)
			res = LoginResult.ON_COOLDOWN;
	}

	@Override
	public void loginSuccess(String ip, String account) {
		listener.loginSuccess(ip, account);
	}

	@Override
	public void loginTry(String ip, String account) {
		listener.loginTry(ip, account);
		if (blocked.contains(ip)) {
			long msDur = Duration.ofHours(config.security.perma_duration).get(ChronoUnit.MILLIS);
			if (Timing.isOver(banEnteringDates.get(ip), msDur)) {
				blocked.remove(ip);
				banEnteringDates.remove(ip);
			} else {
				bannedIPRequest(ip);
				return;
			}
		}
		ipTries.put(ip, ipTries.getOrDefault(ip,0) + 1);
		accountTries.put(account, accountTries.getOrDefault(account,0) + 1);
		if (ipTries.get(ip) >= config.security.login.max_ip_tries) {
			banIP(ip, BanReason.TRY_LIMIT_EXCEEDED);
		}
		if (accountTries.get(account) >= config.security.login.max_acc_tries) {
			alertAccount(account,ip, AccountAlertReason.MAX_TRIES_EXCEED);
		}
	}

	@Override
	public void exceedBasicTries(String ip, String account) {
		listener.exceedBasicTries(ip, account);
		legidGuessCounter.getOrDefault(account, new HashMap<>()).remove(ip);

		Set<String> thisAccountWaintingArea = waitingArea.getOrDefault(account, new HashSet<>());
		thisAccountWaintingArea.add(ip);
		waitingArea.put(account, thisAccountWaintingArea);

		HashMap<String, Long> thiAccountWaitingEnterTimes = waitingAreaEnterTime.getOrDefault(ip, new HashMap<>());
		thiAccountWaitingEnterTimes.put(ip, System.currentTimeMillis());
		waitingAreaEnterTime.put(account, thiAccountWaitingEnterTimes);

		if (thisAccountWaintingArea.size() > config.security.login.waiting_area_stop) {
			alertAccount(account, ip, AccountAlertReason.WAITING_AREA_FULL);
		}
	}

	@Override
	public void guessInWaitingArea(String ip, String account) {
		listener.guessInWaitingArea(ip, account);
		res = LoginResult.WAITING_AREA;
	}

	@Override
	public void alertAccount(String account, String ip, AccountAlertReason reason) {
		listener.alertAccount(account, ip, reason);
		warned.add(account);
	}

	@Override
	public void bannedIPRequest(String ip) {
		listener.bannedIPRequest(ip);
		res = LoginResult.BLOCKED;
	}

	private static class Timing {
		public static long between(long a, long b) {
			return Math.abs(a - b);
		}

		public static boolean isOver(long l) {
			return System.currentTimeMillis() > l;
		}

		public static boolean isOver(long start, long dur) {
			return start + dur < System.currentTimeMillis();
		}
	}
}

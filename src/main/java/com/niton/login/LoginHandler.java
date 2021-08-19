package com.niton.login;

import com.niton.login.cfg.LoginSecurityConfig;
import com.typesafe.config.ConfigFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

public class LoginHandler<PT> implements LoginListener {
	// Account related
	private final Map<String, Integer> accountTries = new HashMap<>();
	private final Set<String>              warned       = new HashSet<>();
	private final Map<String, Long>        accLastRequest      = new HashMap<>();
	private final Map<String, Set<String>> accountAccessingIPs = new HashMap<>();

	//IP related
	private final Set<String> waitingArea         = new HashSet<>();
	private final Map<String, Integer>              ipTries              = new HashMap<>();
	private final Set<String>                           blocked          = new HashSet<>();
	private final Map<String, Long>                 banEnteringDates     = new HashMap<>();
	private final Map<String, Long>                 ipLastRequest        = new HashMap<>();
	private final Map<String, Long>    waitingAreaEnterTime = new HashMap<>();
	private final Map<String, Map<String, Integer>> legidGuessCounter    = new HashMap<>();
	/**
	 * Stores the account each ip accessed {@code <ip, accessedAccounts>}
	 */
	private final Map<String, Set<String>>          ipsAccountAccessing  = new HashMap<>();

	private final LoginSecurityConfig config;
	private final Authenticator<PT>   auther;

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
		loginTry(ip, account); //sets res to BANNED or null
		if (res != null)//-> banned IP
			return res;

		//person isnt banned




		long waitingAreaMS = Duration.ofMinutes(config.security.login.waiting_area_time).toMillis();
		long waitinAreaEnterTime = waitingAreaEnterTime.getOrDefault(ip,0L);
		if (Timing.isOver(waitinAreaEnterTime, waitingAreaMS)) {
			waitingArea.remove(ip);
		} else {
			guessInWaitingArea(ip, account);
			return res;
		}

		//check if there is a cooldown violation
		long lastIPRequest = ipLastRequest.getOrDefault(ip,0L);
		long lastAccRequest = accLastRequest.getOrDefault(account,0L);
		long delayMS = (long) (config.security.login.login_delay * 1000);
		boolean isIpEligibleForRequest = Timing.isOver(lastIPRequest, delayMS);
		boolean isAccEligibleForRequest = Timing.isOver(lastAccRequest, delayMS);

		//the request is counted, no matter if the ip is on cooldown or not
		legidGuessCounter.getOrDefault(account, new HashMap<>()).put(ip,legidGuessCounter.getOrDefault(account, new HashMap<>()).getOrDefault(ip,0 )+1);
		ipLastRequest.put(ip, System.currentTimeMillis());
		accLastRequest.put(account, System.currentTimeMillis());

		if (!isIpEligibleForRequest || !isAccEligibleForRequest) {
			guessInCooldown(ip, account);//changes res to ON_COOLDOWN or BANNED
			return res;
		}
		//the ip is allowed to do a request at the moment

		Map<String, Integer> thisAccountLegitCount = legidGuessCounter.getOrDefault(account, new HashMap<>());
		thisAccountLegitCount.put(ip, thisAccountLegitCount.getOrDefault(ip, 0) + 1);
		legidGuessCounter.put(account, thisAccountLegitCount);

		ipAccessAccountLogin(ip, account);

		if (res != null)
			return res;

		//messing with database by timing determinition
		try {
			Thread.sleep((long) (10+Math.random() * 40));
		} catch (InterruptedException ignored) {
		}
		boolean valid = auther.authenticate(account, password);
		if (valid) {
			loginSuccess(ip, account);
			return LoginResult.VERIFIED;
		} else {
			loginFail(ip, account);
			if (thisAccountLegitCount.get(ip) > config.security.login.tries_bevore_waiting)
				exceedBasicTries(ip, account);
			return LoginResult.UNSUCCESSFULL;
		}
	}

	@Override
	public void banIP(String ip, BanReason reason) {
		listener.banIP(ip, reason);
		for (String account : ipsAccountAccessing.getOrDefault(ip, new HashSet<>())) {
			if(!warned.contains(account))
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

		if (ipsForThisAccount.size() > config.security.login.max_ips_per_account)
			if(!warned.contains(account))
				alertAccount(account, ip, AccountAlertReason.TO_MANY_IPS_ACCESSING);
	}

	@Override
	public void loginFail(String ip, String account) {
		listener.loginFail(ip, account);
	}

	@Override
	public void guessInCooldown(String ip, String account) {
		listener.guessInCooldown(ip, account);
		if (warned.contains(account))
			banIP(ip, BanReason.GUESS_ON_WARNED_ACCOUNT);
		if (res == null)//could be banned too
			res = LoginResult.ON_COOLDOWN;
	}

	@Override
	public void loginSuccess(String ip, String account) {
		ipsAccountAccessing.remove(ip);
		accountAccessingIPs.getOrDefault(account, new HashSet<>()).remove(ip);
		this.legidGuessCounter.getOrDefault(account, new HashMap<>()).remove(ip);
		this.warned.remove(account);
		this.waitingArea.remove(ip);
		this.ipTries.remove(ip);
		listener.loginSuccess(ip, account);
	}

	@Override
	public void loginTry(String ip, String account) {
		listener.loginTry(ip, account);
		if (blocked.contains(ip)) {
			long msDur = Duration.ofHours(config.security.perma_duration).toMillis();
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
			if(!warned.contains(account))
				alertAccount(account,ip, AccountAlertReason.MAX_TRIES_EXCEED);
		}
	}

	@Override
	public void exceedBasicTries(String ip, String account) {
		listener.exceedBasicTries(ip, account);
		legidGuessCounter.getOrDefault(account, new HashMap<>()).remove(ip);

		waitingArea.add(ip);
		waitingAreaEnterTime.put(ip, System.currentTimeMillis());

		if (waitingArea.size() > config.security.login.waiting_area_stop) {
			for(String areaIP : waitingArea)
				alertAccount(account, areaIP, AccountAlertReason.WAITING_AREA_FULL);
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

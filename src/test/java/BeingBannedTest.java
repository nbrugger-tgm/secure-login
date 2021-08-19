import com.niton.login.*;
import org.junit.jupiter.api.Test;

import java.util.logging.Handler;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BeingBannedTest {

	boolean banned = false;
	@Test
	public void bruteForce(){
		LoginHandler<String> testHandler = new LoginHandler<String>(new Authenticator<String>() {
			@Override
			public boolean authenticate(String user, String password) {
				return password.equals("YEET");
			}
		});
		testHandler.setListener(new LoginListener() {
			@Override
			public void banIP(String ip, BanReason reason) {
				banned = true;
			}

			@Override
			public void ipAccessAccountLogin(String ip, String account) {

			}

			@Override
			public void loginFail(String ip, String account) {

			}

			@Override
			public void guessInCooldown(String ip, String account) {

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
			public void alertAccount(String account, String ip, AccountAlertReason reason) {

			}

			@Override
			public void bannedIPRequest(String ip) {

			}
		});
		for(int i = 0; i < 100; i++) {
			testHandler.handle("","Wrong", "");
		}
		assertTrue(banned);
	}
}

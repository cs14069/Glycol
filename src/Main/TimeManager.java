package Main;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class TimeManager {
	private static String startTime;
	private static final String getHealthPath = "/v1/gethealth";

	void init() {
		startTime = getDate();
	}

	static boolean checkTradable() {
		/* mentenance and stop return false */
		String response;

		response = HTTPConnector.access(Constant.Keyword.GET, getHealthPath);
		try {
			if (response.contains("STOP")) {
				return false;
			}
		} catch (NullPointerException e) {
		}

		return true;
	}

	static String getToday() {
		return new SimpleDateFormat("yyyy/MM/dd").format(new Date());
	}

	static String getCurrentTime() {
		return new SimpleDateFormat("HH:mm:ss").format(new Date());
	}

	static String getDate() {
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
	}

//	static String getDate2mBefore() {
//		Calendar cal = Calendar.getInstance();
//		cal.setTime(new Date());
//		cal.add(Calendar.MINUTE, -2);
//		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(cal.getTime());
//	}
}

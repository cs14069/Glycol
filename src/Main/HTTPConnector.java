package Main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HTTPConnector {
	String host = "https://api.bitflyer.jp";

	private OnHttpResponseListener resLis = null;
	private OnHttpErrorListener errLis = null;

	private static int getNum = 0;
	private static int postNum = 0;

	public HTTPConnector(String path) {
		host += path;
	}

	public static String access(String method, String path) {
		return access(method, path, "", "", "");
	}

	public static String access(String method, String path, String APILEY, String APISECRET) {
		return access(method, path, "", APILEY, APISECRET);
	}

	public static String access(String method, String path, String body, String APIKEY, String APISECRET) {
		int[] status = { 0 };
		String[] response = { null };
		if (method == Constant.Keyword.GET) {
			System.err.println("GET [" + ++getNum + "] : path [" + path + "]");
			if (APIKEY != "" && APISECRET != "") {
				HashMap<String, String> header = HTTPConnector.createHeader(method, path, "", APIKEY, APISECRET);
				HTTPConnector.httpConnectGet(path, status, response, header);
			} else {
				HTTPConnector.httpConnectGet(path, status, response);
			}
		} else if (method == Constant.Keyword.POST) {
			System.err.println("POST [" + ++postNum + "] : path [" + path + "]");
			HashMap<String, String> header = HTTPConnector.createHeader(method, path, body, APIKEY, APISECRET);
			HTTPConnector.httpConnectPost(path, status, response, header, body);
		}

		while (response[0] == null && status[0] == 0) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}
		return response[0];
	}

	public void get() {
		get(new HashMap<String, String>());
	}

	public void get(final HashMap<String, String> headerOrg) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final HashMap header = (HashMap) headerOrg.clone();
				HttpURLConnection uc = null;
				try {
					URL url = new URL(host);
					uc = (HttpURLConnection) url.openConnection();
					uc.setRequestProperty("User-Agent", Setting.USERAGENT);
					uc.setConnectTimeout(Constant.Access.TIMEOUT);
					uc.setReadTimeout(Constant.Access.TIMEOUT);
					for (Iterator it = header.entrySet().iterator(); it.hasNext();) {
						Map.Entry entry = (Map.Entry) it.next();
						uc.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
					}
					InputStream is = uc.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(is));

					StringBuffer sb = new StringBuffer();
					String s;
					while ((s = reader.readLine()) != null) {
						sb.append(s);
					}
					if (resLis != null)
						resLis.onResponse(sb.toString());
					reader.close();
				} catch (FileNotFoundException e) {
					System.out.println(e);
				} catch (IOException e) {
					try {
						BufferedReader reader = new BufferedReader(new InputStreamReader(uc.getErrorStream()));
						String s;
						while ((s = reader.readLine()) != null) {
							System.out.println(s);
						}
					} catch (Exception e1) {
						System.out.println("Exception in IOException catch");
						e1.printStackTrace();
					}
					e.printStackTrace();
					if (errLis != null)
						errLis.onError(HTTPConnectorStatusCode.error);
				}
			}
		}).start();
	}

	public void post(final Map<String, String> header, final String data) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					URL url = new URL(host);
					HttpURLConnection uc = (HttpURLConnection) url.openConnection();
					uc.setRequestProperty("User-Agent", Setting.USERAGENT);
					uc.setConnectTimeout(Constant.Access.TIMEOUT);
					uc.setReadTimeout(Constant.Access.TIMEOUT);
					uc.setDoOutput(true);
					uc.setRequestProperty("Content-type", "application/json");
					uc.setRequestMethod("POST");
					for (Iterator it = header.entrySet().iterator(); it.hasNext();) {
						Map.Entry entry = (Map.Entry) it.next();
						uc.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
					}
					OutputStream os = uc.getOutputStream();

					PrintStream ps = new PrintStream(os);
					ps.print(data);
					ps.close();

					InputStream is = uc.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(is));

					StringBuffer sb = new StringBuffer();
					String s;
					while ((s = reader.readLine()) != null) {
						sb.append(s);
					}
					if (resLis != null)
						resLis.onResponse(sb.toString());
					reader.close();

				} catch (IOException e) {
					if (errLis != null)
						errLis.onError(HTTPConnectorStatusCode.error);
				}
			}
		}).start();
	}

	public void setOnHttpResponseListener(OnHttpResponseListener listener) {
		this.resLis = listener;
	}

	public void setOnHttpErrorListener(OnHttpErrorListener listener) {
		this.errLis = listener;
	}

	public static void httpConnectGet(String path, int[] status, String[] response) {
		httpConnectGet(path, status, response, new HashMap<String, String>());
	}

	public static void httpConnectGet(String path, final int[] status, final String[] response,
			HashMap<String, String> header) {
		HTTPConnector httpConnector = new HTTPConnector(path);
		httpConnector.setOnHttpResponseListener(new OnHttpResponseListener() {
			@Override
			public void onResponse(String data) {
				status[0] = Constant.Keyword.DONE;
				response[0] = data;
			}
		});
		httpConnector.setOnHttpErrorListener(new OnHttpErrorListener() {
			@Override
			public void onError(int error) {
				status[0] = Constant.Keyword.HTTP_ERROR;
			}
		});
		httpConnector.get(header);
	}

	public static void httpConnectPost(String path, final int[] status, final String[] response,
			Map<String, String> header, String data) {
		HTTPConnector httpConnector = new HTTPConnector(path);
		httpConnector.setOnHttpResponseListener(new OnHttpResponseListener() {
			@Override
			public void onResponse(String data) {
				status[0] = Constant.Keyword.DONE;
				response[0] = data;
			}
		});
		httpConnector.setOnHttpErrorListener(new OnHttpErrorListener() {
			@Override
			public void onError(int error) {
				status[0] = Constant.Keyword.HTTP_ERROR;
			}
		});
		httpConnector.post(header, data);
	}

	public static HashMap<String, String> createHeader(String method, String path, String body, String APIKEY,
			String APISECRET) {
		HashMap<String, String> header = new HashMap<String, String>();
		String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
		String sign = HMAC.get(timestamp + method + path + body, APISECRET);
		header.put("ACCESS-KEY", APIKEY);
		header.put("ACCESS-TIMESTAMP", timestamp);
		header.put("ACCESS-SIGN", sign);
		return header;
	}
}

class HTTPConnectorStatusCode {
	static final int error = 400;
}

interface OnHttpResponseListener extends EventListener {
	void onResponse(String response);
}

interface OnHttpErrorListener extends EventListener {
	void onError(int error);
}

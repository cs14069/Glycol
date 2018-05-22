package Main;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.arnx.jsonic.JSON;

public class OrderManager {
	private final String sendChildOrder = "/v1/me/sendchildorder";
	private final String getPositionsPath = "/v1/me/getpositions";
	private final String getChildOrdersPath = "/v1/me/getchildorders";
	private final String cancelChildOrder = "/v1/me/cancelchildorder";
	private final String getTickerPath = "/v1/getticker";
	private LogManager lm;
	private String APIKEY = "";
	private String APISECRET = "";
	private int lastParentOrderId = 0;
	private List parentOrderList;

	OrderManager(LogManager lm, String APIKEY, String APISECRET) {
		this.lm = lm;
		this.APIKEY = APIKEY;
		this.APISECRET = APISECRET;
		parentOrderList = new ArrayList<Map>();
	}

	Map getTicker(String productCode) {
		Map map = null;
		String response = HTTPConnector.access(Constant.Keyword.GET, getTickerPath + "?product_code=" + productCode);
		if (response != null) {
			map = (Map) JSON.decode(response);
			lm.log("[OrderManager.getTicker()] response: " + response);
		} else {
			lm.log("![OrderManager.getTicker()] response: null");
		}
		return map;
	}

	double getRestPosition(String productCode) {
		Map map;
		double size = 0;
		String response = HTTPConnector.access(Constant.Keyword.GET, getPositionsPath + "?product_code=" + productCode,
				APIKEY, APISECRET);
		if (response == null) {
			lm.log("![OrderManager.getRestPosition()] response: null");
			return 0;
		}
		List list = (List) JSON.decode(response);
		lm.log("[OrderManager.getRestPosition()] response: " + response);
		if (list.size() == 0) {
			return 0.0;
		}
		for (int i = 0; i < list.size(); i++) {
			map = (Map) list.get(i);
			if (map.get("side").equals(Constant.Keyword.BUY)) {
				size += ((BigDecimal) map.get("size")).doubleValue();
			} else {
				size -= ((BigDecimal) map.get("size")).doubleValue();
			}
		}
		size = Library.getProperSize(size);
		return size;
	}

	List<Map> getOrderList(String productCode, List<Map> activeOrderIdList) throws InterruptedException {
		List<Map> orderList;
		String response, childOrderAcceptanceId;

		orderList = new ArrayList<Map>();
		for (Iterator<Map> it = activeOrderIdList.iterator(); it.hasNext();) {
			Map tmp = it.next();
			childOrderAcceptanceId = (String) tmp.get(Constant.Keyword.CHILD_ORDER_ACCEPTANCE_ID);
			response = HTTPConnector.access(Constant.Keyword.GET,
					getChildOrdersPath + "?" + Constant.Keyword.PRODUCT_CODE + "=" + productCode + "&"
							+ Constant.Keyword.CHILD_ORDER_ACCEPTANCE_ID + "=" + childOrderAcceptanceId,
					APIKEY, APISECRET);
			if(response.equals("[]")) {
				Map map = new HashMap();
				map.put(Constant.Keyword.CHILD_ORDER_ACCEPTANCE_ID, childOrderAcceptanceId);
				map.put(Constant.Keyword.CHILD_ORDER_STATE, Constant.Keyword.DELETED);
				map.put(Constant.Keyword.SIDE, tmp.get(Constant.Keyword.SIDE));
				map.put(Constant.Keyword.AVERAGE_PRICE, tmp.get(Constant.Keyword.AVERAGE_PRICE));
				map.put(Constant.Keyword.OUTSTANDING_SIZE, 0);
				map.put(Constant.Keyword.PRICE, tmp.get(Constant.Keyword.PRICE));
				orderList.add(map);
				lm.log("[OrderManager.getOrderList()] response: " + response);
			} else if(response != null) {
				orderList.add(((List<Map>) JSON.decode(response)).get(0));
				lm.log("[OrderManager.getOrderList()] response: " + response);
			} else {
				lm.log("![OrderManager.getOrderList()] response: null");			
			}
			Thread.sleep(Constant.Access.INTERVAL);
		}

		return orderList;
	}

	void cancelAllOrder(String productCode, List<Map> orderList) throws InterruptedException {
		for (Iterator<Map> it = orderList.iterator(); it.hasNext();) {
			cancelOrder(productCode, it.next());
			Thread.sleep(Constant.Access.INTERVAL);
		}
	}

	void cancelOrder(String productCode, Map order) {
		String json = JSON
				.encode(new CancelOrder(productCode, (String) order.get(Constant.Keyword.CHILD_ORDER_ACCEPTANCE_ID)));
		String response = HTTPConnector.access(Constant.Keyword.POST, cancelChildOrder, json, APIKEY, APISECRET);
		lm.log("[OrderManager.cancelOrder()] response: " + response);
	}

	String orderMarket(String productCode, String side, double size) {
		return order(productCode, side, size, -1);
	}

	String orderLimit(String productCode, String side, double size, int price) {
		return order(productCode, side, size, price);
	}

	private String order(String productCode, String side, double size, int price) {
		String json, response;
		Parameter param = new Parameter();
		if (price < 0) {
			param.setMarketParam(productCode, side, Library.getProperSize(size));
		} else {
			param.setLimitParam(productCode, side, price, Library.getProperSize(size));
		}
		Order order = new Order(Constant.Order.MINUTE_TO_EXPIRE, Constant.Keyword.GTC, param);
		json = JSON.encode(order);
		lm.log("[OrderManager.order()] orderJson: " + json);
		response = HTTPConnector.access(Constant.Keyword.POST, sendChildOrder, json, APIKEY, APISECRET);
		if (response != null) {
			Map map = (Map) JSON.decode(response);
			lm.log("[OrderManager.order()] response: " + response);
			return (String) map.get(Constant.Keyword.CHILD_ORDER_ACCEPTANCE_ID);
		} else {
			lm.log("![OrderManager.order()] response: null");
			return null;
		}
	}
}

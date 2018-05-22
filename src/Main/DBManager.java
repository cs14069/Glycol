package Main;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Main.AssetManager.Asset;

public class DBManager {
	private String server;
	private String dbName;
	private String userName;
	private String password;
	private String botStartDate;
	private String botStartTime;
	private LogManager lm;

	DBManager(LogManager lm, String server, String dbName, String userName, String password, String botStartDate,
			String botStartTime) {
		this.lm = lm;
		this.server = server;
		this.dbName = dbName;
		this.userName = userName;
		this.password = password;
		this.botStartDate = botStartDate;
		this.botStartTime = botStartTime;
	}

	private Connection con = null;
	private Statement st = null;
	private ResultSet rs = null;

	void updateLastBuyOrder() throws DBException {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection("jdbc:mysql://" + server + "/" + dbName, userName, password);
			st = con.createStatement();
			String query = "update orderTable set " + Constant.Keyword.SOLD + " = 0 where " + Constant.Keyword.SOLD
					+ " = -1";
			lm.log("[DBManager.updateLastBuyOrder()] query: " + query);
			st.executeUpdate(query);
			st.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new DBException();
		}
	}

	void addCollateral(String productCode, double restPosition, Asset asset) throws DBException {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection("jdbc:mysql://" + server + "/" + dbName, userName, password);
			st = con.createStatement();
			String query = "insert into collateral values ('" + botStartDate + "', '" + botStartTime + "', "
					+ restPosition + ", " + asset.fx.get(Constant.Keyword.COLLATERAL) + ", "
					+ asset.fx.get(Constant.Keyword.OPEN_POSITION_PNL) + ", "
					+ asset.fx.get(Constant.Keyword.REQUIRE_COLLATERAL) + ", "
					+ asset.fx.get(Constant.Keyword.KEEP_RATE) + ")";
			lm.log("[DBManager.addCollateral()] query: " + query);
			st.executeUpdate(query);

			st.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new DBException();
		}
	}

	void updateOrderList(List<Map> orderList) throws DBException {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection("jdbc:mysql://" + server + "/" + dbName, userName, password);
			for (Iterator<Map> it = orderList.iterator(); it.hasNext();) {
				Map map = it.next();
				// if(map.get(Constant.Keyword.CHILD_ORDER_STATE).equals(Constant.Keyword.ACTIVE))
				// {
				// continue;
				// }
				// 売り注文が約定していたら、それまでの注文（一つ以外を除いて）すべてを完了とする
				st = con.createStatement();
				if (map.get(Constant.Keyword.CHILD_ORDER_STATE).equals(Constant.Keyword.COMPLETED)
						&& map.get(Constant.Keyword.SIDE).equals(Constant.Keyword.SELL)) {
					String query = "update orderTable set " + Constant.Keyword.SOLD + " = " + 1 + " where "
							+ Constant.Keyword.CHILD_ORDER_ACCEPTANCE_ID + " <= '"
							+ map.get(Constant.Keyword.CHILD_ORDER_ACCEPTANCE_ID) + "' and not ("
							+ Constant.Keyword.SOLD + " = -1 and " + Constant.Keyword.SIDE + " = '"
							+ Constant.Keyword.BUY + "')";
					lm.log("[DBManager.updateOrderList()] query(sold): " + query);
					st.executeUpdate(query);
				}
				int price = 0;
				if (map.get(Constant.Keyword.PRICE) instanceof BigDecimal) {
					price = ((BigDecimal) map.get(Constant.Keyword.PRICE)).intValue();
					if (price == 0) {
						price = ((BigDecimal) map.get(Constant.Keyword.AVERAGE_PRICE)).intValue();
					}
				} else {
					// orderをAPIで取得した結果[]だったから
					// DBから得た値を入れている
					price = (int) map.get(Constant.Keyword.PRICE);
					// これはあり得ない
					if (price == 0) {
						price = (int) map.get(Constant.Keyword.AVERAGE_PRICE);
					}
				}
				String query = "update orderTable set " + Constant.Keyword.CHILD_ORDER_STATE + " = '"
						+ map.get(Constant.Keyword.CHILD_ORDER_STATE) + "', " + Constant.Keyword.AVERAGE_PRICE + "="
						+ price + ", " + Constant.Keyword.OUTSTANDING_SIZE + " = "
						+ map.get(Constant.Keyword.OUTSTANDING_SIZE) + " where "
						+ Constant.Keyword.CHILD_ORDER_ACCEPTANCE_ID + " = '"
						+ map.get(Constant.Keyword.CHILD_ORDER_ACCEPTANCE_ID) + "'";
				lm.log("[DBManager.updateOrderList()] query: " + query);
				st.executeUpdate(query);
				st.close();
			}
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new DBException();
		}

	}

	void addLog(int cost, double sellRate) throws DBException {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection("jdbc:mysql://" + server + "/" + dbName, userName, password);
			st = con.createStatement();
			String query = "insert into log values('" + botStartDate + "', '" + botStartTime + "', " + cost + ", "
					+ sellRate + ")";
			lm.log("[DBManager.addLog()] query: " + query);
			st.executeUpdate(query);

			st.close();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new DBException();
		}
	}

	int getLastBuyOrderRate() throws DBException {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection("jdbc:mysql://" + server + "/" + dbName, userName, password);
			st = con.createStatement();
			String query = "select " + Constant.Keyword.AVERAGE_PRICE + " from orderTable where "
					+ Constant.Keyword.SIDE + " = '" + Constant.Keyword.BUY + "' order by "
					+ Constant.Keyword.CHILD_ORDER_ACCEPTANCE_ID + " desc limit 1";
			lm.log("[DBManager.getLastBuyOrderRate()] query: " + query);
			rs = st.executeQuery(query);
			if (rs.first()) {
				return rs.getInt(1);
			} else {
				return 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DBException();
		}
	}

	List<Map> getOrderList(String childOrderState) throws DBException {
		List<Map> activeOrderList;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection("jdbc:mysql://" + server + "/" + dbName, userName, password);
			st = con.createStatement();
			String query = "select * from orderTable where sold <= 0 and " + Constant.Keyword.CHILD_ORDER_STATE + " = '"
					+ childOrderState + "'";
			lm.log("[DBManager.getOrderIdList()] query: " + query);
			rs = st.executeQuery(query);
			activeOrderList = new ArrayList<Map>();
			while (rs.next()) {
				Map map = new HashMap();
				map.put(Constant.Keyword.CHILD_ORDER_ACCEPTANCE_ID,
						rs.getString(Constant.Keyword.CHILD_ORDER_ACCEPTANCE_ID));
				map.put(Constant.Keyword.SIDE, rs.getString(Constant.Keyword.SIDE));
				map.put(Constant.Keyword.AVERAGE_PRICE, rs.getInt(Constant.Keyword.AVERAGE_PRICE));
				map.put(Constant.Keyword.OUTSTANDING_SIZE, rs.getDouble(Constant.Keyword.OUTSTANDING_SIZE));
				map.put(Constant.Keyword.PRICE, rs.getInt(Constant.Keyword.AVERAGE_PRICE));
				activeOrderList.add(map);
			}
			return activeOrderList;
		} catch (Exception e) {
			e.printStackTrace();
			throw new DBException();
		}
	}

	void addOrder(String side, String childOrderAcceptanceId, double amount, int price) throws DBException {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection("jdbc:mysql://" + server + "/" + dbName, userName, password);
			st = con.createStatement();
			String query = "insert into orderTable values ('" + botStartDate + "', '" + botStartTime + "', '" + side
					+ "', " + amount + ", " + amount + ", " + price + ", '" + childOrderAcceptanceId + "', '"
					+ Constant.Keyword.ACTIVE + "', -1)";
			lm.log("[DBManager.addOrder()] query: " + query);
			st.executeUpdate(query);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DBException();
		}
	}

	int getCurrentOrderTimes() throws DBException {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection("jdbc:mysql://" + server + "/" + dbName, userName, password);
			st = con.createStatement();
			String query = "select count(*) as count from orderTable where " + Constant.Keyword.SOLD + " <= 0 and "
					+ Constant.Keyword.SIDE + " = '" + Constant.Keyword.BUY + "'";
			lm.log("[DBManager.getCurrentOrderTimes()] query: " + query);
			rs = st.executeQuery(query);
			rs.next();
			return rs.getInt("count");
		} catch (Exception e) {
			e.printStackTrace();
			throw new DBException();
		}
	}
}

class DBException extends Exception {

}
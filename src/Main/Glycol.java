package Main;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.arnx.jsonic.JSON;

public class Glycol extends Thread {

	public static void main(String[] args) {
		if(args.length == 1) {
			Thread thread = new Glycol(args[0]);
			thread.start();
		} else {
			Thread thread = new Glycol("");
			thread.start();
		}
	}
	private boolean TRADE = false;
	
	public Glycol(String arg) {
		if(arg.equals("trade")) {
			System.err.println("TRADE");
			TRADE = true;
		} else {
			System.err.println("ONLY LOG");
			TRADE = false;
		}
	}

	public void run() {
		LogManager lm;
		AssetManager asm;
		OrderManager om;
		DBManager dm;
		String productCode, sellOrderId, buyOrderId, botStartDate, botStartTime;
		double orderAmount, restPosition, lastBuyOrderRate;
		int currentPrice, cost, sellRate;
		
		List<Map> activeOrderList;
		try {
			sellOrderId = null;
			buyOrderId = null;
			botStartDate = TimeManager.getToday();
			botStartTime = TimeManager.getCurrentTime();
			lm = new LogManager(Constant.Log.LOG_FILE_PATH, botStartDate);
			lm.log("init: " + botStartDate + " " + botStartTime);

			asm = new AssetManager(lm, Setting.APIKEY, Setting.APISECRET);
			om = new OrderManager(lm, Setting.APIKEY, Setting.APISECRET);
			dm = new DBManager(lm, Setting.DB_SERVER, Setting.LOG_DB, Setting.DB_USER, Setting.DB_PASS, botStartDate, botStartTime);

			productCode = Constant.Keyword.FX_BTC_JPY;

			// APIで資産を取得
			asm.updateFxAsset();

			// APIでポジションを取得
			restPosition = om.getRestPosition(productCode);
			Thread.sleep(Constant.Access.INTERVAL);
			
			// ACTIVEな注文をDBから取ってきて、APIで確認して、DBをアップデート
			checkActiveOrderList(dm, om, productCode);
			
			// DBに証拠金を記録
			dm.addCollateral(productCode, restPosition, asm.getAsset());
			
			if(!TRADE) {
				return;
			}
			
			// 前回までのポジでかかったコスト
			cost = (int) Math.ceil(dm.getCurrentOrderTimes() * Constant.Order.EACH_ORDER_COST);
			
			// 前回までのポジションの利確価格
			sellRate = (int) Math.ceil(cost/restPosition*Constant.Order.PROFIT_RATE);

			// DBに前回までのログを記録
			dm.addLog(cost, sellRate);
			
			// 最後の買い注文価格
			lastBuyOrderRate = dm.getLastBuyOrderRate();
			
			// バグなどにより最後の買い注文より低い価格で売ろうとしていたら終了
			if(sellRate < lastBuyOrderRate) {
				lm.log("[Glycol.run()] Abort: Invalid sellRate(" + sellRate + ")");
			}

			
			// 現在の価格を取得 
			currentPrice = ((BigDecimal)om.getTicker(productCode).get(Constant.Keyword.BEST_ASK)).intValue();
			
			// 注文する量を算出
			orderAmount = Library.getProperSize(Constant.Order.EACH_ORDER_COST / (double)currentPrice);
			
			
			// DBからもう一度アクティブな注文のIDを取得
			activeOrderList = dm.getOrderList(Constant.Keyword.ACTIVE);
			
			// 売り注文のみを抽出 買い注文を削除
			for(Iterator<Map> it = activeOrderList.iterator(); it.hasNext(); ) {
				String side = (String)it.next().get(Constant.Keyword.SIDE);
				if(side.equals(Constant.Keyword.BUY)) {
					it.remove();
				}
			}
			
			// 古い売り注文をすべてキャンセル
			om.cancelAllOrder(productCode, activeOrderList);
			
			// DBのsoldが-1のところを0にする
			dm.updateLastBuyOrder();

			
			if(sellRate > 0) {
				// 前回までのポジションを売る 注文できなかったら(nullなら)再トライ
				for(int i = 0; i < Constant.Access.MAX_RETRY && sellOrderId == null; i++) {
					sellOrderId = om.orderLimit(productCode, Constant.Keyword.SELL, restPosition, sellRate);
					Thread.sleep(Constant.Access.INTERVAL);
				}

				// 注文できていれば注文IDをデータベースへ
				if(sellOrderId != null) {
					dm.addOrder(Constant.Keyword.SELL, sellOrderId, restPosition, sellRate);					
				}
			}
			
			// 新しいポジションを買う 注文できなかったら(nullなら)再トライ
			for(int i = 0; i < Constant.Access.MAX_RETRY && buyOrderId == null; i++) {
				buyOrderId = om.orderMarket(productCode, Constant.Keyword.BUY, orderAmount);				
				Thread.sleep(Constant.Access.INTERVAL);
			}

			// 注文できていれば注文IDをデータベースへ
			// 成行注文なのでpriceはとりあえず現在価格を入れておく 次の実行で書き換えられる
			if(buyOrderId != null) {
				dm.addOrder(Constant.Keyword.BUY, buyOrderId, orderAmount, currentPrice);				
			}

			
			// 注文してすぐには反映されないようなので、"[]"が返ってきて"DELETED"になってしまう
			// ACTIVEな注文をDBから取ってきて、APIで確認して、DBをアップデート
			// checkActiveOrderList(dm, om, productCode);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	void checkActiveOrderList(DBManager dm, OrderManager om, String productCode) throws Exception{
		List<Map> orderList;
		List<Map> activeOrderList;
		// DBにある注文の中で、ACTIVEな注文のID
		activeOrderList = dm.getOrderList(Constant.Keyword.ACTIVE);

		// APIで注文一覧の状況を取得
		orderList = om.getOrderList(productCode, activeOrderList);
		
		// DBの注文リストをアップデート
		dm.updateOrderList(orderList);
	}
}

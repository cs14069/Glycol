package Main;

public class Constant {

	class Access {
		private Access() {}
		public static final int INTERVAL = 1000; // 適所にsleepを記述して使う APIアクセス後のインターバル
		public static final int TIMEOUT = 20*1000; // APIアクセスのタイムアウト
		public static final int MAX_RETRY = 3; // 適所にfor文を記述して使う APIアクセスの最大リトライ数
	}
	
	class Log {
		private Log() {}
		public static final String LOG_FILE_PATH = "log/";
	}
	class Order {
		private Order() {}
		public static final int EACH_ORDER_COST = 20000; // 20000円分買う
		public static final int MINUTE_TO_EXPIRE = 60*3 - 5;	// 3時間-5分経ったら注文が消される
		public static final double PROFIT_RATE = 1.02; // 102%で売る
	}
	
	class Keyword {
		private Keyword() {}
		// http
		public static final String GET = "GET";
		public static final String POST = "POST";
		public static final int DONE = 200;
		public static final int HTTP_ERROR = 400;
		
		// collateral
		public static final String COLLATERAL =  "collateral";
		public static final String OPEN_POSITION_PNL = "open_position_pnl";
		public static final String REQUIRE_COLLATERAL = "require_collateral";
		public static final String KEEP_RATE = "keep_rate";
		public static final String OUTSTANDING_SIZE = "outstanding_size";

		// order
		public static final String CURRENCY_CODE = "currency_code";
		public static final String AMOUNT = "amount";
		public static final String AVAILABLE = "available";
		public static final String FX_BTC_JPY = "FX_BTC_JPY";
		public static final String BUY = "BUY";
		public static final String SELL = "SELL";
		public static final String SIMPLE = "SIMPLE";
		public static final String GTC = "GTC";
		public static final String COMPLETED = "COMPLETED";
		public static final String ACTIVE = "ACTIVE";
		public static final String DELETED = "DELETED";
		public static final String CHILD_ORDER_ACCEPTANCE_ID = "child_order_acceptance_id";
		public static final String PRODUCT_CODE = "product_code";
		public static final String CHILD_ORDER_STATE = "child_order_state";
		public static final String AVERAGE_PRICE = "average_price";
		public static final String PRICE = "price";
		public static final String SIDE = "side";
		
		
		// ticker
		public static final String BEST_ASK = "best_ask";
		
		// db
		public static final String SELL_RATE = "sell_rate"; 
		public static final String LOG_DATE = "log_date"; 
		public static final String LOG_TIME = "log_time";
		public static final String SOLD = "sold";
		
	}
}

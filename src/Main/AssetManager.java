package Main;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.arnx.jsonic.JSON;

public class AssetManager {
	private LogManager lm;
	private String APIKEY = "";
	private String APISECRET = "";
	private final String getBalancePath = "/v1/me/getbalance";
	private final String getCollateralPath = "/v1/me/getcollateral";
	private Asset asset;
	
	AssetManager(LogManager lm, String APIKEY, String APISECRET) {
		this.lm = lm;
		this.APIKEY = APIKEY;
		this.APISECRET = APISECRET;
		asset = new Asset();
	}
	void updateActualAsset() {
		String response;
		response = HTTPConnector.access(Constant.Keyword.GET, getBalancePath, APIKEY, APISECRET);
		
		List<Map> list = (List) JSON.decode(response);
		for(Iterator<Map> it = list.iterator(); it.hasNext(); ) {
			Map map = it.next();
			asset.amount.put((String) map.get(Constant.Keyword.CURRENCY_CODE), ((BigDecimal)map.get(Constant.Keyword.AMOUNT)).doubleValue());
			asset.available.put((String) map.get(Constant.Keyword.CURRENCY_CODE), ((BigDecimal)map.get(Constant.Keyword.AVAILABLE)).doubleValue());
		}
		
		lm.log("[Update Asset]: "+asset.amount.toString() + "(Available: "+ asset.available.toString() + ")");
	}
	
	void updateFxAsset() {
		String response;
		response = HTTPConnector.access(Constant.Keyword.GET, getCollateralPath, APIKEY, APISECRET);
		
		Map list = (Map)JSON.decode(response);
		for(Iterator it = list.entrySet().iterator(); it.hasNext(); ) {
		    Map.Entry entry = (Map.Entry)it.next();
		    asset.fx.put((String) entry.getKey(), ((BigDecimal) entry.getValue()).doubleValue());
		}
		
		lm.log("[Update Asset(FX)]: "+asset.fx.toString());
	}
	Asset getAsset() {
		return asset;
	}

	class Asset {
		Map<String, Double> amount;
		Map<String, Double> available;
		Map<String, Double> fx;
		Asset() {
			amount = new HashMap<String, Double>();
			available = new HashMap<String, Double>();
			fx = new HashMap<String, Double>();
		}
	}

}

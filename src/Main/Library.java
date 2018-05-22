package Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Library {
	public static String fileReadAll(String path) {
		StringBuilder builder = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			String string = reader.readLine();
			while (string != null) {
				builder.append(string + System.getProperty("line.separator"));
				string = reader.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		return builder.toString();
	}

	public static boolean fileWrite(String path, String content, boolean append) {
		try {
			File file = new File(path);

			if (file.isFile() && file.canWrite()) {
				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, append)));
				pw.print(content);
				pw.close();
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean createFile(String path) {
		try {
			File file = new File(path);
			file.createNewFile();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static double getProperSize(double size) {
		return (double) Math.round(size * 100000000) / 100000000;
	}
}

class Order {
	public String product_code;
	public String child_order_type;
	public String side;
	public int price;
	public double size;
	public int minute_to_expire;
	public String time_in_force;	

	Order(int mte, String tif, Parameter p) {
		minute_to_expire = mte;
		time_in_force = tif;
		product_code = p.product_code;
		child_order_type = p.child_order_type;
		side = p.side;
		price = p.price;
		size = p.size;
	}
}

class Parameter {
	public String product_code;
	public String child_order_type;
	public String side;
	public int price;
	public double size;

	void setLimitParam(String pc, String sd, int p, double sz) {
		child_order_type = "LIMIT";
		product_code = pc;
		side = sd;
		price = p;
		size = sz;
	}
	void setMarketParam(String pc, String sd, double sz) {
		child_order_type = "MARKET";
		product_code = pc;
		side = sd;
		size = sz;
	}
}

class CancelOrder {
	public String product_code;
	public String child_order_acceptance_id;

	CancelOrder(String pc, String coai) {
		product_code = pc;
		child_order_acceptance_id = coai;
	}
}
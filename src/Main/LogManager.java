package Main;
public class LogManager {
	private String logFilePath;
	private String fileName;
	LogManager(String logFilePath, String fileName) {
		this.logFilePath = logFilePath;
		this.fileName = fileName.replaceAll("[/:]", "_");
		if (!makeLogFile()) {
			throw new Error();
		}
		String content = "\n\n\n        -----------------------------\n---- START AUTO TRADE AGENT " + TimeManager.getCurrentTime()
				+ " ----\n        -----------------------------";
		log(content);
	}
	
	boolean makeLogFile() {
		if (Library.createFile(logFilePath+fileName)
				&& Library.fileWrite(logFilePath+fileName, "", true)) {
			return true;
		}
		return false;
	}

	boolean log(String content) {
		content += "\n";
		return Library.fileWrite(logFilePath+fileName, content, true);
	}
}


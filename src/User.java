import java.io.PrintWriter;

public class User {

	private PrintWriter writer = null;
	private AES key = null;
	
	User(PrintWriter writer, AES key){
		this.writer = writer;
		this.key = key;
	}

	public PrintWriter getWriter() {
		return writer;
	}

	public void setWriter(PrintWriter writer) {
		this.writer = writer;
	}

	public AES getKey() {
		return key;
	}

	public void setKey(AES key) {
		this.key = key;
	}
	
}

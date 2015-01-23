package main.java.com.milev.app;
public class MsgAlreadyTerminated extends Exception {

	private static final long serialVersionUID = 1L;

	public MsgAlreadyTerminated() { super("This message group was already terminated!"); }
}

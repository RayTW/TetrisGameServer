package raytw.server;

public interface CmdProcListener {
	public void offline(User user);
	
	public void onReadCommand(User user, CharSequence str);
}

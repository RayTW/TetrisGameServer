package raytw.server;

public interface CmdProcListener {
  /**
   * user離線.
   *
   * @param user user
   */
  public void offline(User user);

  /**
   * 讀取到的user訊息.
   *
   * @param user user
   * @param msg 訊息
   */
  public void onReadCommand(User user, String msg);
}

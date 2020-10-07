package raytw.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;

public class PoolManager {
  private static PoolManager instance = new PoolManager();
  private ConcurrentHashMap<ChannelId, ChannelHandlerContext> channelHandlerContextPool;
  private ConcurrentHashMap<ChannelId, User> userPool;

  private PoolManager() {
    channelHandlerContextPool = new ConcurrentHashMap<>();
    userPool = new ConcurrentHashMap<>();
  }

  public static PoolManager get() {
    return instance;
  }

  public void putChannel(ChannelId id, ChannelHandlerContext handler) {
    channelHandlerContextPool.put(id, handler);
  }

  public ChannelHandlerContext getChannel(ChannelId id) {
    return channelHandlerContextPool.get(id);
  }

  public ChannelHandlerContext removeChannel(ChannelId id) {
    return channelHandlerContextPool.remove(id);
  }

  public void putUser(ChannelId id, User handler) {
    userPool.put(id, handler);
  }

  public User getUser(ChannelId id) {
    return userPool.get(id);
  }

  public User removeUser(ChannelId id) {
    return userPool.remove(id);
  }

  public void write(JSONObject json, User user) {
    write(json.toString(), user);
  }

  /**
   * 發送訊息給指定的User.
   *
   * @param json 訊息
   * @param users user
   */
  public void write(JSONObject json, List<User> users) {
    String msg = json.toString();
    users
        .stream()
        .forEach(
            user -> {
              PoolManager.get().write(msg, user);
            });
  }

  /**
   * 發送訊息給指定的User.
   *
   * @param msg 訊息
   * @param user user
   */
  public void write(String msg, User user) {
    ByteBuf buf = Unpooled.wrappedBuffer(msg.getBytes(StandardCharsets.UTF_8));
    ChannelHandlerContext channel = getChannel(user.getChannelId());
    if (channel != null) {
      channel.writeAndFlush(buf);
    }
  }

  /**
   * 發送訊息給指定的User.
   *
   * @param msg 訊息
   * @param users user
   */
  public void write(String msg, List<User> users) {
    users
        .stream()
        .forEach(
            user -> {
              PoolManager.get().write(msg, user);
            });
  }

  public void writeExcluded(JSONObject json, List<User> users, User excluded) {
    writeExcluded(json.toString(), users, excluded);
  }

  /**
   * 發送訊息給指定的User並且排除指定user.
   *
   * @param msg 訊息
   * @param users user
   */
  public void writeExcluded(String msg, List<User> users, User excluded) {
    users
        .stream()
        .filter(u -> !u.getName().equals(excluded.getName()))
        .forEach(
            user -> {
              PoolManager.get().write(msg, user);
            });
  }
}

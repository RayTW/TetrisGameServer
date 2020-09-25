package raytw.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class PoolManager {
  private static PoolManager instance = new PoolManager();
  private ConcurrentHashMap<ChannelId, ChannelHandlerContext> channelHandlerContextPool;
  private ConcurrentHashMap<ChannelId, User> userPool;

  private PoolManager() {
    channelHandlerContextPool = new ConcurrentHashMap<>();
    userPool = new ConcurrentHashMap<>();
  }

  public static PoolManager getInstance() {
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

  public void write(String msg, User user) {
    ByteBuf buf = Unpooled.wrappedBuffer(msg.getBytes(StandardCharsets.UTF_8));
    ChannelHandlerContext channel = getChannel(user.getChannelId());

    if (channel != null) {
      channel.writeAndFlush(buf);
    }
  }
}

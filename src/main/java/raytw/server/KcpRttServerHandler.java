package raytw.server;

import io.jpower.kcp.netty.UkcpChannel;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.nio.charset.StandardCharsets;
import raytw.util.Debug;

public class KcpRttServerHandler extends ChannelInboundHandlerAdapter {
  private CmdProcListener listener;
  private int conv;

  public KcpRttServerHandler(int conv, CmdProcListener listener) {
    this.conv = conv;
    this.listener = listener;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    UkcpChannel kcpCh = (UkcpChannel) ctx.channel();
    kcpCh.conv(conv);

    User user = new User();
    user.setChannelId(kcpCh.id());

    PoolManager.get().putChannel(kcpCh.id(), ctx);
    PoolManager.get().putUser(kcpCh.id(), user);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    ChannelId id = ctx.channel().id();
    PoolManager.get().removeChannel(id);
    User user = PoolManager.get().removeUser(id);

    if (user != null) {
      listener.offline(user);
    }
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    ByteBuf buf = (ByteBuf) msg;
    CharSequence str = buf.getCharSequence(0, buf.capacity(), StandardCharsets.UTF_8);
    User user = PoolManager.get().getUser(ctx.channel().id());
    listener.onReadCommand(user, str.toString());
    buf.release();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    // Close the connection when an exception is raised.
    System.out.println("server.exceptionCaught,ctx=" + ctx + ",\n" + Debug.get().toString(cause));
    ctx.close();
  }
}

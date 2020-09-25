package raytw.server;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import io.jpower.kcp.netty.UkcpChannel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class KcpRttClientHandler extends ChannelInboundHandlerAdapter {
  private final ByteBuf data;

  /** Creates a client-side handler. */
  public KcpRttClientHandler(int count) {
    data = Unpooled.buffer(count);
    for (int i = 0; i < data.capacity(); i++) {
      data.writeByte((byte) i);
    }
  }

  @Override
  public void channelActive(final ChannelHandlerContext ctx) {
    UkcpChannel kcpCh = (UkcpChannel) ctx.channel();
    kcpCh.conv(KcpRttClient.CONV); // set conv
    System.out.println("xx11channelActive,ctx=" + ctx);

    ByteBuf obj = Unpooled.wrappedBuffer("name".getBytes(StandardCharsets.UTF_8));

    ctx.writeAndFlush(obj);
    System.out.println("xx22channelActive,ctx=" + ctx);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    System.out.println("channelInactive,ctx=" + ctx);
  }

  @Override
  public void channelRead(final ChannelHandlerContext ctx, Object msg) {
    ByteBuf buf = (ByteBuf) msg;
    CharSequence str = buf.getCharSequence(0, buf.capacity(), StandardCharsets.UTF_8);
    System.out.println("channelRead<-" + str);
    buf.release();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    // Close the connection when an exception is raised.
    System.out.println("exceptionCaught,ctx=" + ctx);
    ctx.close();
  }
}

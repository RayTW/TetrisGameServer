package raytw.server;

import io.jpower.kcp.netty.ChannelOptionHelper;
import io.jpower.kcp.netty.UkcpChannel;
import io.jpower.kcp.netty.UkcpChannelOption;
import io.jpower.kcp.netty.UkcpServerChannel;
import io.netty.bootstrap.UkcpServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONObject;
import raytw.server.room.Room;
import raytw.server.room.RoomManager;
import raytw.util.Debug;

public class TetrisGameServer implements CmdProcListener {
  private static final int CONV = Integer.parseInt(System.getProperty("conv", "10"));
  private static final int PORT = Integer.parseInt(System.getProperty("port", "8009"));

  private RoomManager roomManager = new RoomManager();

  /**
   * 啟動Server.
   *
   * @throws InterruptedException 服務中止
   */
  public void start() throws InterruptedException {
    // Configure the server.
    EventLoopGroup group = new NioEventLoopGroup();
    try {
      UkcpServerBootstrap b = new UkcpServerBootstrap();
      b.group(group)
          .channel(UkcpServerChannel.class)
          .childHandler(
              new ChannelInitializer<UkcpChannel>() {
                @Override
                public void initChannel(UkcpChannel ch) throws Exception {
                  ChannelPipeline p = ch.pipeline();
                  p.addLast(new KcpRttServerHandler(CONV, TetrisGameServer.this));
                }
              });
      ChannelOptionHelper.nodelay(b, true, 20, 2, true)
          .childOption(UkcpChannelOption.UKCP_MTU, 512);

      // Start the server.
      InetSocketAddress address = new InetSocketAddress(PORT);
      ChannelFuture f = b.bind(address).sync();

      System.out.println(
          "Server running, ip: " + address.getAddress().toString() + ",port:" + PORT);
      try {
        address.getAddress();
        System.out.println("local ip: " + InetAddress.getLocalHost() + ",port:" + PORT);
      } catch (UnknownHostException e) {
        e.printStackTrace();
      }

      // Wait until the server socket is closed.
      f.channel().closeFuture().sync();
    } finally {
      // Shut down all event loops to terminate all threads.
      group.shutdownGracefully();
    }
  }

  @Override
  public void offline(User user) {
    Debug.get().println("user[" + user.getName() + "] offline");
    roomManager.userOffline(user);
  }

  @Override
  public void onReadCommand(User user, String str) {
    JSONObject json = new JSONObject(str);
    int code = json.getInt("code");

    /*
     * {
     *   "code": 1,
     *   "name": "ray"
     * }
     */
    if (code == 1) {
      user.setName(json.optString("name", ""));
      roomManager.addMatching(user);
      return;
    }

    /*
     * {
     *   "code": 411,
     *   "roomId": "3b1848b0-fad0-4967-824a-ac9540f49be7",
     *   "operation": {
     *      "event": 2,
     *      "simulation":false
     *  }
     * }
     */
    if (code == 411) {
      Room room = roomManager.getRoom(user.getRoomId());

      if (room != null) {
        room.boradcastKeyCode(user, json.getJSONObject("operation"));
      }
    }
  }

  /**
   * Server程式進入點.
   *
   * @param args 參數
   */
  public static void main(String[] args) {
    PropertyConfigurator.configure("config//log4j.properties");
    try {
      new TetrisGameServer().start();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}

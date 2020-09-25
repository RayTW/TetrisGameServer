package raytw.server;

import io.netty.channel.ChannelId;

public class User {
  private ChannelId channelId;
  private String name = "";

  public ChannelId getChannelId() {
    return this.channelId;
  }

  public void setChannelId(ChannelId channelId) {
    this.channelId = channelId;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }
}

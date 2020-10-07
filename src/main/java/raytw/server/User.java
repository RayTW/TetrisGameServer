package raytw.server;

import io.netty.channel.ChannelId;

public class User {
  private ChannelId channelId;
  private String roomId;
  private String name = "";

  public ChannelId getChannelId() {
    return this.channelId;
  }

  public void setChannelId(ChannelId channelId) {
    this.channelId = channelId;
  }

  public void setRoomId(String roomId) {
    this.roomId = roomId;
  }

  public String getRoomId() {
    return roomId;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof User) {
      return name.equals(((User) o).name);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}

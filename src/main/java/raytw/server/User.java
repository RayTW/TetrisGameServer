package raytw.server;

import io.netty.channel.ChannelId;

public class User {
  private ChannelId channelId;
  private String name = "";
  private String roomId;
  private int position;

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

  public void setRoomId(String roomId) {
    this.roomId = roomId;
  }

  public String getRoomId() {
    return roomId;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public int getPosition() {
    return position;
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

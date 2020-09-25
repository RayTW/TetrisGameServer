package raytw.server;

import java.util.concurrent.CopyOnWriteArrayList;

public class Room {
  private CopyOnWriteArrayList<User> users;

  public Room() {
    users = new CopyOnWriteArrayList<>();
  }

  public void addUser(User user) {
    users.add(user);
  }

  public void boradcast(int keycode) {
    users
        .stream()
        .forEach(
            user -> {
              // PoolManager.getInstance().write("同步方塊", user);
            });
  }
}

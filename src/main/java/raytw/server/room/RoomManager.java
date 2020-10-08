package raytw.server.room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import raytw.server.PoolManager;
import raytw.server.User;

public class RoomManager {
  private List<User> matchingPool;
  private ConcurrentHashMap<String, Room> rooms;
  private Thread thread;
  private boolean isMatching;
  // 遊戲每個房間配對人數
  private int matchesSize = 2;

  /** 初始化. */
  public RoomManager() {
    isMatching = true;
    rooms = new ConcurrentHashMap<>();
    matchingPool = Collections.synchronizedList(new LinkedList<User>());
    thread = new Thread(this::matching);
    thread.start();
  }

  private void matching() {
    ArrayList<User> list = new ArrayList<>();

    while (isMatching) {
      // user配對
      while (matchingPool.size() >= matchesSize) {
        for (int i = 0; i < matchesSize && matchingPool.size() > 0; i++) {
          User user = matchingPool.remove(0);

          list.add(user);
        }
        // 人數滿足創房條件，將user加入遊戲房
        if (list.size() == matchesSize) {
          String roomId = UUID.randomUUID().toString();
          Room room = new Room(roomId, matchesSize);

          list.forEach(
              user -> {
                user.setRoomId(roomId);
                room.addUser(user);
              });

          // 註冊遊戲房關閉後移除事件傾聽
          room.setCloseListener(rooms::remove);
          rooms.put(roomId, room);
        } else {
          list.forEach(o -> matchingPool.add(0, o));
        }
        list.clear();
      }

      try {
        TimeUnit.MILLISECONDS.sleep(500);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      //      Debug.get()
      //          .println(
      //              "配對中人數:" + matchingPool.size() + System.lineSeparator() + "遊戲房間數:" +
      // rooms.size());
    }
  }

  /**
   * 將user加入配對佇列.
   *
   * @param user user
   */
  public void addMatching(User user) {
    JSONObject ret = new JSONObject();
    ret.put("code", 2);

    if (user.getName().isEmpty()) {
      ret.put("addUser", false);
      ret.put("message", "name empty");
      PoolManager.get().write(ret, user);
      return;
    }

    if (matchingPool.contains(user)) {
      ret.put("addUser", false);
      ret.put("message", "repeat matching");
      PoolManager.get().write(ret, user);
      return;
    }

    ret.put("addUser", matchingPool.add(user));

    /*
     * {
     *   "code": 2,
     *   "addUser": false,
     *   "message": "name empty"
     * }
     */
    PoolManager.get().write(ret, user);
  }

  public Room getRoom(String roomId) {
    return rooms.get(roomId);
  }

  /**
   * user離線.
   *
   * @param user user
   */
  public void userOffline(User user) {
    Room room = rooms.get(user.getRoomId());

    if (room != null) {
      room.userOffline(user);
    }
  }
}

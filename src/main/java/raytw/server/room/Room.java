package raytw.server.room;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;
import raytw.server.PoolManager;
import raytw.server.User;

public class Room {
  private String id;
  private CopyOnWriteArrayList<User> users;
  private CountDownLatch firer;
  private Thread looper;
  private RoomState state;
  private boolean isRunning;
  private Consumer<String> closeListener;

  /**
   * 建立遊戲房.
   *
   * @param id 遊戲房id
   * @param fireCount 啟動遊戲的人數
   */
  public Room(String id, int fireCount) {
    this.id = id;
    users = new CopyOnWriteArrayList<>();
    firer = new CountDownLatch(fireCount);
    state = RoomState.INIT;

    isRunning = true;
    looper =
        new Thread(
            () -> {
              try {
                firer.await();
              } catch (InterruptedException e) {
                e.printStackTrace();
              }
              while (isRunning) {

                gameFlow();

                try {
                  TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                  e.printStackTrace();
                }
              }
            });
    looper.start();
  }

  public void setCloseListener(Consumer<String> listener) {
    closeListener = listener;
  }

  /**
   * 將從配對池選中的玩家加入遊戲室.
   *
   * @param user 玩家
   * @return
   */
  public boolean addUser(User user) {
    if (users.add(user)) {
      user.setPosition(users.indexOf(user));

      JSONObject json = new JSONObject();

      json.put("code", 300);
      json.put("roomId", id);
      json.put("position", user.getPosition());
      JSONArray ary = new JSONArray();

      IntStream.range(0, users.size())
          .forEach(
              idx -> {
                User u = users.get(idx);
                JSONObject userInfo = new JSONObject();

                userInfo.put("position", idx);
                userInfo.put("name", u.getName());

                ary.put(userInfo);
              });

      json.put("users", ary);

      /*
       * {
       *   "code": 300,
       *   "roomId": "3b1848b0-fad0-4967-824a-ac9540f49be7",
       *   "position": 0,
       *   "users": [
       *       {
       *         "position": 0,
       *         "name": "user1"
       *       }
       *    ]
       * }
       */
      PoolManager.get().write(json, user);
      firer.countDown();
      return true;
    }
    return false;
  }

  private void gameFlow() {
    if (state == RoomState.INIT) {
      state = RoomState.RUNNING;
      JSONObject json = new JSONObject();

      json.put("code", 400);
      json.put("message", "game start");

      PoolManager.get().write(json, users);
    }
    if (state == RoomState.INIT || state == RoomState.RUNNING) {
      if (users.size() == 0) {
        state = RoomState.FINISH;
      }
      return;
    }
    if (state == RoomState.FINISH) {
      close();
      return;
    }
  }

  /**
   * 廣播目前的user操作給其他user同步畫面.
   *
   * @param user 目前操作的user
   * @param operation 遊戲操作
   */
  public void boradcastKeyCode(User user, JSONObject operation) {
    JSONObject json = new JSONObject();

    json.put("code", 412);
    json.put("position", user.getPosition());
    json.put("operation", operation);

    /*
     * {
     *   "code": 412,
     *   "roomId": "3b1848b0-fad0-4967-824a-ac9540f49be7",
     *   "operation": {
     *      "event": 2,
     *      "style":false
     *  }
     * }
     */
    PoolManager.get().writeExcluded(json, users, user);
  }

  /**
   * 處理user斷線.
   *
   * @param user usesr
   */
  public void userOffline(User user) {
    users.remove(user);
  }

  /** 關閉遊戲房. */
  public void close() {
    isRunning = false;

    if (closeListener != null) {
      closeListener.accept(id);
    }
  }
}

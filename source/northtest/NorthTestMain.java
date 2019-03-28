package northtest;

import static north.network.NetworkDefinitions.*;
import north.North;

public class NorthTestMain {

   public static void main(String[] params) throws Exception {
      North.init("test", 1, 1, TestIDriveAndNav.INSTANCE);

      while(true) {
         North.sendMessage(null, Message, "test message");
         North.tick();
         Thread.sleep(10);
      }
   }
}
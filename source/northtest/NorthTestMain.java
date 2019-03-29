package northtest;

import static north.network.NetworkDefinitions.*;
import north.North;
import north.parameters.Parameter;

public class NorthTestMain {
   public static Parameter param_a = new Parameter(null, "test parameter");
   public static Parameter v_max = new Parameter("Profile Test", "Max Velocity");

   public static void main(String[] params) throws Exception {
      North.init("test", 1, 1, TestIDriveAndNav.INSTANCE);

      while(true) {
         North.sendMessage(null, Message, "test message");
         North.tick();
         Thread.sleep(10);
      }
   }
}
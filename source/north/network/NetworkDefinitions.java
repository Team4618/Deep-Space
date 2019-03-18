package north.network;

import north.North;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map.Entry;

public class NetworkDefinitions {
   //NOTE: north_common_definitions.h
   static final byte GameMode_Disabled = 0;
   static final byte GameMode_Autonomous = 1;
   static final byte GameMode_Teleop = 2;
   static final byte GameMode_Test = 3;
   
   public static final byte Unitless = 0;
   public static final byte Feet = 1;
   public static final byte FeetPerSecond = 2;
   public static final byte Degrees = 3;
   public static final byte DegreesPerSecond = 4;
   public static final byte Seconds = 5;
   public static final byte Percent = 6;
   public static final byte Amp = 7;
   public static final byte Volt = 8;
   
   public static final byte Message = 0;
   public static final byte Warning = 1;
   public static final byte Error = 2;
   public static final byte SubsystemOffline = 3;
   
   static final byte CommandType_NonBlocking = 0;
   static final byte CommandType_Blocking = 1;
   static final byte CommandType_Continuous = 2;

   //NOTE: north_network_definitions.h
   static final byte SetConnectionFlags = 1; //<-
   static final byte Welcome = 2; //->
   static final byte CurrentParameters = 3; //->
   static final byte State = 4; //->
   static final byte ParameterOp = 5;  //<-
   static final byte SetState = 6;  //<-
   static final byte UploadAutonomous = 7;  //<-

   public static ByteBuffer createWelcomePacket() {
      ByteBuffer result = ByteBuffer.allocate(16384); //TODO: this is rlly big
      result.put(Welcome);

      result.put((byte) North.name.length()); //u8 robot_name_length;
      //TODO: trim result so we're not sending the full 16K

      return result;
   }

   public static ByteBuffer createCurrentParametersPacket() {
      ByteBuffer result = ByteBuffer.allocate(0);
      result.put(State);

      return result;
   }

   public static ByteBuffer createStatePacket() {
      ByteBuffer result = ByteBuffer.allocate(0);
      result.put(State);

      return result;
   }
}

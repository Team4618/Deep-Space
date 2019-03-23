package north.network;

import north.North;
import north.North.NorthGroup;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class NetworkDefinitions {
   //----north_common_definitions.h------------------------
   //North_GameMode
   static final byte GameMode_Disabled = 0;
   static final byte GameMode_Autonomous = 1;
   static final byte GameMode_Teleop = 2;
   static final byte GameMode_Test = 3;
   
   //North_Unit
   public static final byte Unitless = 0;
   public static final byte Feet = 1;
   public static final byte FeetPerSecond = 2;
   public static final byte Degrees = 3;
   public static final byte DegreesPerSecond = 4;
   public static final byte Seconds = 5;
   public static final byte Percent = 6;
   public static final byte Amp = 7;
   public static final byte Volt = 8;
   
   //North_MessageType
   public static final byte Message = 0;
   public static final byte Warning = 1;
   public static final byte Error = 2;
   public static final byte SubsystemOffline = 3;
   
   //North_CommandExecutionType
   static final byte CommandExecutionType_NonBlocking = 0;
   static final byte CommandExecutionType_Blocking = 1;
   static final byte CommandExecutionType_Continuous = 2;

   //North_HermiteControlPoint
   //North_PathDataPoint

   //North_CommandType
   static final byte CommandType_Generic = 1;
   static final byte CommandType_Wait = 2;
   static final byte CommandType_Pivot = 3;

   //----north_network_definitions.h------------------------
   //PacketType
   static final byte SetConnectionFlags = 1; //<-
   static final byte Welcome = 2;            //->
   static final byte CurrentParameters = 3;  //->
   static final byte State = 4;              //->
   static final byte ParameterOp = 5;        //<-
   static final byte SetState = 6;           //<-
   static final byte UploadAutonomous = 7;   //<-

   //SetConnectionFlags_Flags
   static final byte ConnectionFlag_WANTS_STATE = (1 << 0);

   public static ByteBuffer createWelcomePacket() {
      ByteBuffer data = ByteBuffer.allocate(16384); //TODO: this is rlly big
      
      //Welcome_PacketHeader
      data.put((byte) North.name.length()); //u8 robot_name_length;
      data.put((byte) 0); //u8 conditional_count;
      data.put((byte) 0); //u8 command_count;
      data.putFloat((byte) North.width); //f32 robot_width;
      data.putFloat((byte) North.length); //f32 robot_length;
      data.putInt((int) 0); //u32 flags;
   
      data.put(North.name.getBytes()); //char name[robot_name_length]

      //TODO: conditionals
      //{ u8 length; char [length]; } [conditional_count]

      //TODO: commands
      //Welcome_Command [command_count]

      //TODO: trim result so we're not sending the full 16K

      //new Packet(data, Welcome) //this can add the packet header to the begining 
      return data;
   }

   public static void writeCurrentParameters_Group(ByteBuffer data, String name, NorthGroup group) {
      data.put((byte) name.length()); //u8 name_length;
      data.put((byte) (group.parameter_arrays.size() + group.parameters.size())); //u8 param_count;
      
      //TODO: write parameters
   }

   public static ByteBuffer createCurrentParametersPacket() {
      ByteBuffer data = ByteBuffer.allocate(16384);
      Set<Map.Entry<String, NorthGroup>> groups = North.groups.entrySet();

      //CurrentParameters_PacketHeader
      data.put((byte) groups.size()); //u8 group_count;

      writeCurrentParameters_Group(data, "", North.default_group); //CurrentParameters_Group default_group
      for(Map.Entry<String, NorthGroup> group : groups) { //CurrentParameters_Group [group_count]
         writeCurrentParameters_Group(data, group.getKey(), group.getValue());   
      }

      return data;
   }

   public static ByteBuffer createStatePacket() {
      ByteBuffer result = ByteBuffer.allocate(0);
      result.put(State);

      return result;
   }
}

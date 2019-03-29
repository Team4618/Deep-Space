package north.network;

import north.Diagnostics;
import north.North;
import north.North.NorthGroup;
import north.parameters.Parameter;
import north.parameters.ParameterArray;
import north.util.NorthUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
   static final byte Heartbeat = 0;          // <->
   static final byte SetConnectionFlags = 1; // <-
   static final byte Welcome = 2;            //  ->
   static final byte CurrentParameters = 3;  //  ->
   static final byte State = 4;              //  ->
   static final byte ParameterOp = 5;        // <-
   static final byte SetState = 6;           // <-
   static final byte UploadAutonomous = 7;   // <-

   //SetConnectionFlags_Flags
   static final byte ConnectionFlag_WANTS_STATE = (1 << 0);

   public static ByteBuffer PackPacket(ByteBuffer data, byte type) {
      int size = data.position();
      ByteBuffer result = ByteBuffer.allocate(size + 4 + 1);
      result.order(ByteOrder.LITTLE_ENDIAN);
      
      //PacketHeader
      result.putInt(size); //Size
      result.put(type); //Type
      
      result.put(data.array(), 0, size);
      return result;
   }

   public static ByteBuffer createWelcomePacket() {
      ByteBuffer data = ByteBuffer.allocate(16384); //TODO: this is rlly big
      data.order(ByteOrder.LITTLE_ENDIAN);
      
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

      return PackPacket(data, Welcome);
   }
   
   public static void writeCurrentParameters_Group(ByteBuffer data, String name, NorthGroup group) {
      data.put((byte) name.length()); //u8 name_length;
      data.put((byte) (group.parameter_arrays.size() + group.parameters.size())); //u8 param_count;
      
      data.put(name.getBytes()); //char name[name_length]

      //CurrentParameters_Parameter [param_count]
      for(Map.Entry<String, Parameter> param : group.parameters.entrySet()) {
         //CurrentParameters_Parameter
         data.put((byte) 0); //u8 is_array;
         data.put((byte) param.getKey().length()); //u8 name_length;
         data.put((byte) 0); //u8 value_count; //Ignored if is_array is false
         data.put(param.getKey().getBytes()); //char name[name_length]
         data.putFloat((float) param.getValue().get()); // f32 [value_count]
      }

      for(Map.Entry<String, ParameterArray> param : group.parameter_arrays.entrySet()) {
         //CurrentParameters_Parameter
         data.put((byte) 1); //u8 is_array;
         data.put((byte) param.getKey().length()); //u8 name_length;
         data.put((byte) param.getValue().values.length); //u8 value_count; //Ignored if is_array is false
         data.put(param.getKey().getBytes()); //char name[name_length]
         
         // f32 [value_count]
         for(Double _val : param.getValue().values) {
            double val = (double) _val;
            data.putFloat((float) val); 
         }
      }
   }

   public static ByteBuffer createCurrentParametersPacket() {
      ByteBuffer data = ByteBuffer.allocate(16384);
      data.order(ByteOrder.LITTLE_ENDIAN);

      Set<Map.Entry<String, NorthGroup>> groups = North.groups.entrySet();

      //CurrentParameters_PacketHeader
      data.put((byte) groups.size()); //u8 group_count;

      writeCurrentParameters_Group(data, "", North.default_group); //CurrentParameters_Group default_group
      for(Map.Entry<String, NorthGroup> group : groups) { //CurrentParameters_Group [group_count]
         writeCurrentParameters_Group(data, group.getKey(), group.getValue());   
      }

      return PackPacket(data, CurrentParameters);
   }

   public static void writeState_Group(ByteBuffer data, String name, NorthGroup group) {
      data.put((byte) name.length()); //u8 name_length;
      data.put((byte) group.pending_diagnostics.size()); // u8 diagnostic_count;
      data.put((byte) group.pending_messages.size()); // u8 message_count;
      data.put((byte) group.pending_markers.size()); // u8 marker_count;
      data.put((byte) group.pending_paths.size()); // u8 path_count;
      
      data.put(name.getBytes()); //char name[name_length]

      //State_Diagnostic [diagnostic_count]
      for(Map.Entry<String, Diagnostics.StateDiagnostic> entry : group.pending_diagnostics.entrySet()) {
         // State_Diagnostic
         data.put((byte) entry.getKey().length()); //u8 name_length;
         data.putFloat((float) entry.getValue().value); //f32 value;
         data.put(entry.getValue().unit); //u8 unit; //NOTE: North_Unit
         data.put(entry.getKey().getBytes()); //char name[name_length]
      }

      //State_Message [message_count]
      for(Diagnostics.StateMessage entry : group.pending_messages) {
         // State_Message
         data.put(entry.type); //u8 type; //NOTE: North_MessageType
         data.putShort((short) entry.text.length()); //u16 length;

         data.put(entry.text.getBytes()); //char message[length]
      }

      //State_Marker [marker_count]
      for(Diagnostics.StateMarker entry : group.pending_markers) {
         // State_Marker
         data.putFloat((float) entry.pos.x); //v2 pos;
         data.putFloat((float) entry.pos.y);
         data.putShort((short) entry.text.length()); //u16 length;
         data.put(entry.text.getBytes()); //char message[length]
      }

      //State_Path [path_count]
      //TODO: finsh this
      for(Diagnostics.StatePath entry : group.pending_paths) {
         // State_Path
         data.putShort((short) entry.text.length()); //u16 length;
         data.put((byte) entry.path.length); //u8 control_point_count;
         data.put(entry.text.getBytes()); //char message[length]
         
         //North_HermiteControlPoint [control_point_count]
         
      }
   }

   public static ByteBuffer createStatePacket() {
      ByteBuffer data = ByteBuffer.allocate(16384);
      data.order(ByteOrder.LITTLE_ENDIAN);

      //State_PacketHeader
      data.putFloat((float) North.state.posx); //v2 pos;
      data.putFloat((float) North.state.posy);
      data.putFloat((float) North.state.angle); //f32 angle;
      data.put(GameMode_Disabled); //u8 mode; //NOTE: North_GameMode

      Set<Map.Entry<String, NorthGroup>> groups = North.groups.entrySet();
      
      data.put((byte) groups.size()); //u8 group_count;
      data.putFloat((float) NorthUtils.getTimestamp()); //f32 time;

      writeState_Group(data, "", North.default_group); //State_Group default_group;
      for(Map.Entry<String, NorthGroup> group : groups) { //State_Group [group_count]
         writeState_Group(data, group.getKey(), group.getValue());   
      }

      return PackPacket(data, State);
   }
}

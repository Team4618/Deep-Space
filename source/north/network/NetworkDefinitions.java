package north.network;

import north.North;
import north.Subsystem;
import north.autonomous.commands.Command;

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
   static final byte Ping = 0;
   static final byte Connect = 1;
   static final byte Welcome = 2;
   static final byte CurrentParameters = 3;
   static final byte State = 4;
   static final byte SetParameter = 5;
   static final byte SetState = 6;
   static final byte CurrentAutoPath = 7;
   static final byte UploadAutonomous = 8;

   public static ByteBuffer createWelcomePacket() {
      ByteBuffer result = ByteBuffer.allocate(16384); //TODO: this is rlly big
      result.put(Welcome);

      result.put((byte) North.name.length()); //u8 robot_name_length;
      result.put((byte) North.subsystems.size()); //u8 subsystem_count;
      result.putFloat((float) North.width); //f32 robot_width;
      result.putFloat((float) North.length); //f32 robot_height;
      result.putInt(0); //u32 flags;

      result.put(North.name.getBytes(Charset.defaultCharset())); //char name[robot_name_length]

      //Welcome_SubsystemDescription [subsystem_count]
      for(Subsystem subsystem : North.subsystems.values()) {
         result.put((byte) subsystem.name().length()); //u8 name_length;
         result.put((byte) subsystem.commands.values().size()); //u8 command_count;
         result.put(subsystem.name().getBytes(Charset.defaultCharset())); //char name[name_length]

         //Welcome_SubsystemCommand [command_count]
         for(Entry<String, Command> command : subsystem.commands.entrySet()) {
               result.put((byte) command.getKey().length()); //u8 name_length;
               result.put((byte) command.getValue().parameterNames.length); //u8 param_count;

               if(command.getValue().isBlocking()) {
                  result.put(CommandType_Blocking); //u8 type;
               } else if(command.getValue().isContinuous()) {
                  result.put(CommandType_Continuous); //u8 type;
               } else {
                  result.put(CommandType_NonBlocking); //u8 type;
               }

               result.put(command.getKey().getBytes(Charset.defaultCharset())); //char name[name_length]

               //{ u8 length; char [length]; } [param_count]
               for(String param : command.getValue().parameterNames) {
                  result.put((byte) param.length());
                  result.put(param.getBytes(Charset.defaultCharset()));
               }
         }
      }

      //TODO: trim result so we're not sending the full 16K

      return result;
   }

   public static ByteBuffer createStatePacket() {
      ByteBuffer result = ByteBuffer.allocate(0);
      result.put(State);

      return result;
   }
}

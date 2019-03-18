package north;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

import edu.wpi.first.wpilibj.Timer;
import north.autonomous.IExecutable;
// import north.autonomous.Node;
import north.drivecontroller.IDriveController;
import north.network.Network;
import north.util.Button;
import north.util.Vector2;

public class North {
   public static double lastTime;
   public static RobotState state;

   public static String name;
   public static double width;
   public static double length;
   public static IDriveAndNavigation drive;

   //NOTE: width & length are in ft 
   public static void init(String _name, double _width, double _length, IDriveAndNavigation _drive) {
      name = _name;
      width = _width;
      length = _length;
      drive = _drive; 
      
      lastTime = Timer.getFPGATimestamp();
      Network.init();
   }

   //NOTE: these only get called while enabled
   public static ArrayList<Runnable> periodic_functions = new ArrayList<>();
   public static void addPeriodic(Runnable func) {
      periodic_functions.add(func);
   }

   public static ArrayList<Runnable> diagnostics_functions = new ArrayList<>();
   public static void addDiagnosticsCallback(Runnable func) {
      diagnostics_functions.add(func);
   }

   public static void tick() {
      Button.tickAll();
      double newTime = Timer.getFPGATimestamp();
      state = drive.getState(newTime - lastTime);
      lastTime = newTime;
      
      diagnostics_functions.forEach(Runnable::run);
      Network.tick();
   }

   public static IDriveController default_drive_controller = null;

   //NOTE: execution stuff
   public static IExecutable current_executable = null;
   public static IDriveController curr_drive_controller = null;

   public static void tickExecution() {
      IDriveController new_controller = null;

      if(current_executable != null) {
         new_controller = current_executable.getDriveController();
         
         IExecutable new_exec = current_executable.execute();
         if((new_exec != null) && (new_exec != current_executable)) {
            new_exec.init();
         }
         current_executable = new_exec;
      }

      if(new_controller == null) {
         new_controller = default_drive_controller;
         assert(default_drive_controller != null);
      }

      if(new_controller != curr_drive_controller) {
         new_controller.init();
      }

      curr_drive_controller = new_controller;
      curr_drive_controller.periodic();

      periodic_functions.forEach(Runnable::run);
   }

   public static boolean executionDone() {
      return (current_executable == null);
   }

   public static void stopExecution() {
      current_executable = null;
   }

   public static void execute(IExecutable exec) {
      if(exec != null)
         current_executable = exec;
   }

   // public static Node auto_starting_node = null;

   //NOTE: diagnostics stuff
   public static void sendMessage(String group, byte type, String text) {
      Diagnostics.getOrCreateGroup(group).pending_messages.add(new Diagnostics.StateMessage(text, type));
   } 
   
   public static void sendMarker(String group, Vector2 pos, String text) {
      Diagnostics.getOrCreateGroup(group).pending_markers.add(new Diagnostics.StateMarker(text, pos));
   }

   public static void sendPath(String group, String text, Vector2... points) {
      Diagnostics.getOrCreateGroup(group).pending_paths.add(new Diagnostics.StatePath(text, points));
   } 

   public static void sendDiagnostic(String group, String name, byte unit, double value) {
      Diagnostics.getOrCreateGroup(group).pending_diagnostics.put(name, new Diagnostics.StateDiagnostic(unit, value));
   }

   public static void sendCurrentParameters() {
      // subsystems.values().forEach(s -> 
      //    s.params.forEach((name, param) -> {
      //       //TODO
      //    }
      // ));
   }
}
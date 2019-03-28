package north;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;

import north.autonomous.IExecutable;
// import north.autonomous.Node;
import north.drivecontroller.IDriveController;
import north.network.Network;
import north.parameters.Parameter;
import north.parameters.ParameterArray;
import north.util.Button;
import north.util.NorthUtils;
import north.util.Vector2;

public class North {
   //NOTE: if this is false dont use things that only exist on the robot (eg. PowerDistributionPanel or Timer)
   public static final boolean IS_REAL = false;

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
      
      lastTime = NorthUtils.getTimestamp();
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
      double newTime = NorthUtils.getTimestamp();
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

   public static class NorthGroup {
      public ArrayList<Diagnostics.StateMessage> pending_messages = new ArrayList<>();
      public ArrayList<Diagnostics.StateMarker> pending_markers = new ArrayList<>();
      public ArrayList<Diagnostics.StatePath> pending_paths = new ArrayList<>();
      public HashMap<String, Diagnostics.StateDiagnostic> pending_diagnostics = new HashMap<>();

      public HashMap<String, Parameter> parameters = new HashMap<>();
      public HashMap<String, ParameterArray> parameter_arrays = new HashMap<>();
   }

   public static NorthGroup default_group = new NorthGroup();
   public static HashMap<String, NorthGroup> groups = new HashMap<>();

   public static NorthGroup getOrCreateGroup(String name) {
      if(name == null) {
         return default_group;
      } else {
         if(!groups.containsKey(name)) {
            groups.put(name, new NorthGroup());
         }

         return groups.get(name);
      }  
   }
   
   //NOTE: diagnostics stuff
   public static void sendMessage(String group, byte type, String text) {
      getOrCreateGroup(group).pending_messages.add(new Diagnostics.StateMessage(text, type));
   } 
   
   public static void sendMarker(String group, Vector2 pos, String text) {
      getOrCreateGroup(group).pending_markers.add(new Diagnostics.StateMarker(text, pos));
   }

   public static void sendPath(String group, String text, Vector2... points) {
      getOrCreateGroup(group).pending_paths.add(new Diagnostics.StatePath(text, points));
   } 

   public static void sendDiagnostic(String group, String name, byte unit, double value) {
      getOrCreateGroup(group).pending_diagnostics.put(name, new Diagnostics.StateDiagnostic(unit, value));
   }

   public static void sendCurrentParameters() {
      //TODO
   }
}
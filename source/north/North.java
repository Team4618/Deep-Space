package north;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import edu.wpi.first.wpilibj.Timer;
import north.autonomous.IExecutable;
import north.autonomous.Node;
import north.network.Network;
import north.reflection.Logic;
import north.util.Button;
import north.util.Vector2;

public class North {
   public static double lastTime;

   public static RobotState state;
   
   //TODO: replace robot state with this?
   // public static RobotPose state;
   // public static EncoderData encoder;

   public static String name;
   public static double width;
   public static double length;
   public static IDriveAndNavigation drive;

   public static HashMap<String, Subsystem> subsystems = new HashMap<>();
   public static HashMap<String, Method> logic = new HashMap<>();

   //NOTE: width & length are in ft 
   public static void init(String _name, double _width, double _length, Class<?> logicProvider, IDriveAndNavigation _drive) {
      name = _name;
      width = _width;
      length = _length;
      drive = _drive; 
      
      if(logicProvider != null) {
         for(Method m : logicProvider.getDeclaredMethods()) {
            if(m.isAnnotationPresent(Logic.class) && 
               Modifier.isStatic(m.getModifiers()))
            {
               if((m.getReturnType() == boolean.class) && (m.getParameterCount() == 0)) {
                  logic.put(m.getName(), m);
               } else {
                  System.err.println("Logic Provider " + m.getName() + ": incorrect signature");
               }
            }
         }
      }

      subsystems.values().forEach(Subsystem::initSystem);
      lastTime = Timer.getFPGATimestamp();
      Network.init();
   }

   public static boolean getConditionValue(String condition) {
      if(logic.containsKey(condition)) {
         Method conditionFunc = logic.get(condition);
         try {
            Object res = conditionFunc.invoke(null);
            if(res instanceof Boolean)
               return (Boolean) res;
         } catch (Exception e) { e.printStackTrace(); }
      } else {
         System.err.println("Condition " + condition + " not found");
      }
      return false;
   }

   public static void tick() {
      Button.tickAll();
      double newTime = Timer.getFPGATimestamp();
      state = drive.getState(newTime - lastTime);
      lastTime = newTime;
      
      Network.tick();
   }

   public static void tickSubsystems() {
      subsystems.values().forEach(s -> {
         if(s.periodicEnabled)
            s.periodic();
      });
   }

   //NOTE: execution queue stuff
   public static Queue<IExecutable> execution_queue = new LinkedList<IExecutable>();
   public static IExecutable current_executable = null;

   public static void tickExecution() {
      drive.setAutomaticControl(true);

      if(current_executable == null) {
         current_executable = execution_queue.poll();
      }

      if(current_executable != null) {
         current_executable = current_executable.execute();
      }
   }

   public static boolean executionDone() {
      return (current_executable == null) && execution_queue.isEmpty();
   }

   public static void stopExecution() {
      current_executable = null;
      execution_queue.clear();
   }

   public static void execute(IExecutable exec) {
      if(exec != null)
         execution_queue.add(exec);
   }

   public static Node auto_starting_node = null;

   //NOTE: diagnostics stuff
   public static void sendMessage(byte type, String text) {

   } 
   
   public static void sendMarker(Vector2 pos, String text) {

   }

   public static void sendPath(String text, Vector2... points) {

   } 


   public static void sendCurrentParameters() {
      subsystems.values().forEach(s -> 
         s.params.forEach((name, param) -> {
            //TODO
         }
      ));
   }
}
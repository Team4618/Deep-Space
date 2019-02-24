package north;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import north.autonomous.commands.Command;
import north.autonomous.CommandState;

public abstract class Subsystem {
   public abstract void init();
   public void updateParameters() {}
   public void periodic() {}
   public void sendDiagnostics() {}
   public abstract String name();
   public void reset() {}
   // public boolean isSafe() {} //TODO: safety stuff

   public boolean periodicEnabled = true;
   public HashMap<String, Command> commands = new HashMap<>();
   public HashMap<String, Object> params = new HashMap<>();

   public Subsystem() { North.subsystems.put(name(), this); }
   
   public void initSystem() {
      for(Field param_variable : this.getClass().getDeclaredFields()) {
         if((param_variable.getType() == north.reflection.Parameter.class) || 
            (param_variable.getType() == north.reflection.ParameterArray.class))
         {
            try {
               param_variable.setAccessible(true);
               String name = param_variable.getName();
               Constructor constructor = param_variable.getType().getConstructor(String.class); 
               Object param_obj = constructor.newInstance("S" + this.name() + "_P" + name + ".param_array");
               param_variable.set(this, param_obj);
               params.put(name, param_obj);
            } catch(Exception e) { e.printStackTrace(); }
         }
      }
      
      for(Method function : this.getClass().getDeclaredMethods()) {
         if(function.isAnnotationPresent(north.reflection.Command.class)) {
               ArrayList<String> params = new ArrayList<>();
               boolean error = false;

               Parameter[] parameters = function.getParameters();
               for(int i = 0; i < parameters.length; i++) {
                  Parameter param = parameters[i];
                  if((i == 0) && (param.getType() == CommandState.class)) {

                  } else if(param.getType() == double.class) {
                     params.add(param.getName());
                  } else {
                     System.err.println(name() + ":" + function.getName() + ": Invalid type " + param.getType() + ":" + param.getName());
                     error = true;
                  }
               }

               if((function.getReturnType() != boolean.class) && (function.getReturnType() != void.class)) {
                  System.err.println(name() + ":" + function.getName() + ": Invalid return type " + function.getReturnType() + ", must be boolean or void");
                  error = true;
               }

               if(!error)
                  commands.put(function.getName(), new Command(this, function, params.toArray(new String[0])));
         }
      }

      init();
   }

   public void sendDiagnostic(String name, byte unit, double value) {
      
   }
}
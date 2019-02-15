package north.autonomous;

import java.util.ArrayList;

import north.North;
import north.autonomous.commands.ICommandInstance;

public class Node implements IExecutable {
   Path in_path;
   ArrayList<Path> out_paths = new ArrayList<>();
   
   ArrayList<ICommandInstance> commands;
   int currently_executing = 0;

   @Override
   public IExecutable execute() {
      if(currently_executing < commands.size()) {
         if(commands.get(currently_executing).invoke()) {
            currently_executing++;
         }
         return this;
     }
      
      for(Path out_path : out_paths) {
         if((out_path.condition == null) || North.getConditionValue(out_path.condition)) {
            return new PivotExecutable(out_path.initial_angle, out_path);
         }
      }

      return null;
   }

   public void reset() {
      currently_executing = 0;
      commands.forEach(ICommandInstance::reset);
      out_paths.forEach(Path::reset);
   }
}
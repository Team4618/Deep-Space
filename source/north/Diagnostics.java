package north;

import java.util.ArrayList;
import java.util.HashMap;

import north.util.Vector2;

public class Diagnostics {
   public static class StateMessage {
      String text;
      byte type;

      public StateMessage(String _text, byte _type) {
         text = _text;
         type = _type;
      }
   }

   public static class StateMarker {
      String text;
      Vector2 pos;

      public StateMarker(String _text, Vector2 _pos) {
         text = _text;
         pos = _pos;
      }
   }

   public static class StatePath {
      String text;
      Vector2[] path;

      public StatePath(String _text, Vector2[] _path) {
         text = _text;
         path = _path;
      }
   }

   public static class StateDiagnostic {
      byte unit;
      double value;

      public StateDiagnostic(byte _unit, double _value) {
         unit = _unit;
         value = _value;
      }
   }

   public static class StateGroup {
      ArrayList<StateMessage> pending_messages = new ArrayList<>();
      ArrayList<StateMarker> pending_markers = new ArrayList<>();
      ArrayList<StatePath> pending_paths = new ArrayList<>();
      HashMap<String, StateDiagnostic> pending_diagnostics = new HashMap<>();
   }

   public static StateGroup default_group = new StateGroup();
   public static HashMap<String, StateGroup> state_groups = new HashMap<>();

   public static StateGroup getOrCreateGroup(String name) {
      if(name == null) {
         return default_group;
      } else {
         if(!state_groups.containsKey(name)) {
            state_groups.put(name, new StateGroup());
         }

         return state_groups.get(name);
      }  
   }   
}
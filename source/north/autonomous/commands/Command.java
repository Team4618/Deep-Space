package north.autonomous.commands;

import north.Subsystem;
import north.autonomous.CommandState;

import java.lang.reflect.Method;

public class Command {
    Subsystem subsystem;
    Method command;
    public String[] parameterNames;

    public Command(Subsystem subsystem, Method command, String[] parameterNames) {
        this.subsystem = subsystem;
        this.command = command;
        this.parameterNames = parameterNames;
    }

    public boolean takesCommandState() {
        Class[] paramTypes = command.getParameterTypes();
        if(paramTypes.length > 0) {
            return paramTypes[0] == CommandState.class;
        }
        return false;
    }

    public boolean isBlocking() {
        return command.getReturnType() == boolean.class;
    }

    public boolean isContinuous() {
        Class[] paramTypes = command.getParameterTypes();
        return (paramTypes.length == 1) && (paramTypes[0] == double.class) && !isBlocking();
    }

    public boolean invoke(CommandState state, double[] in_parameters) {
        try {
            boolean takesState = takesCommandState();

            Object[] parameters = new Object[in_parameters.length + (takesState ? 1 : 0)];
            if(takesState) {
                parameters[0] = state;
            }
            for(int i = 0; i < parameters.length; i++) {
                parameters[i + (takesState ? 1 : 0)] = in_parameters[i];
            }

            Object result = command.invoke(subsystem, parameters);
            if(result instanceof Boolean)
                return (Boolean) result;
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
}

package org.ssw;

import com.sun.jdi.*;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.request.ClassPrepareRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class Debugger {
    private final Class<?> debuggee;
    private final Set<Integer> breakpoints;
    private VirtualMachine virtualMachine;
    private ClassType classType;

    public Debugger(final Class<?> debuggee, final Set<Integer> breakpoints) {
        this.debuggee = requireNonNull(debuggee);
        this.breakpoints = requireNonNull(breakpoints);
    }

    public VirtualMachine start() throws IllegalConnectorArgumentsException, VMStartException, IOException {
        LaunchingConnector launchingConnector = Bootstrap.virtualMachineManager().defaultConnector();
        Map<String, Connector.Argument> arguments = launchingConnector.defaultArguments();
        arguments.get("main").setValue(debuggee.getName());
        virtualMachine = launchingConnector.launch(arguments);

        ClassPrepareRequest classPrepareRequest = virtualMachine.eventRequestManager().createClassPrepareRequest();
        classPrepareRequest.addClassFilter(debuggee.getName());
        classPrepareRequest.enable();

        return virtualMachine;
    }

    public void handleClassPrepareEvent(final ClassPrepareEvent classPrepareEvent) throws AbsentInformationException {
        classType = (ClassType) classPrepareEvent.referenceType();
        for (int lineNumber : breakpoints) {
            addBreakpoint(lineNumber);
        }
    }

    public void addBreakpoint(int lineNumber) throws AbsentInformationException {
        List<Location> locations = classType.locationsOfLine(lineNumber);
        if (locations.isEmpty()) {
            System.out.println("No code location found at line " + lineNumber);
            return;
        }

        virtualMachine.eventRequestManager().createBreakpointRequest(locations.get(0)).enable();
    }
}
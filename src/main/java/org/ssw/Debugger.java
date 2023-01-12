package org.ssw;

import com.sun.jdi.*;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.request.ClassPrepareRequest;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class Debugger {
    private final Class<?> debuggee;
    private final Set<Integer> breakpoints;
    private VirtualMachine virtualMachine;

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
        ClassType classType = (ClassType) classPrepareEvent.referenceType();
        for (int lineNumber : breakpoints) {
            Location location = classType.locationsOfLine(lineNumber).get(0);
            virtualMachine.eventRequestManager().createBreakpointRequest(location).enable();
        }
    }
}
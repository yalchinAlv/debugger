package org.ssw;

import com.sun.jdi.*;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.StepRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException, IllegalConnectorArgumentsException, VMStartException, IOException, InterruptedException, AbsentInformationException, IncompatibleThreadStateException {
        if (args.length == 0) {
            System.err.println("Please provide a class to debug. Usage: Main <debuggee> [<debuggee-args>, ..]");
            System.exit(1);
        }

        final Scanner scanner = new Scanner(System.in);

        final String debuggeeName = args[0];
        System.out.println("Starting debugging " + debuggeeName);
        final Set<Integer> breakpoints = getInitialBreakpoints(scanner);

        final Debugger debugger = new Debugger(Class.forName(debuggeeName), breakpoints);
        final VirtualMachine virtualMachine = debugger.start();

        try {
            while (true) {
                final EventSet events = virtualMachine.eventQueue().remove();
                if (events == null) {
                    break;
                }

                for (final Event event : events) {
                    if (event instanceof VMDisconnectEvent) {
                        System.out.println("Debugging is done. Printing the output of the debuggee:");
                        final BufferedReader debuggeeOutputReader = new BufferedReader(new InputStreamReader(virtualMachine.process().getInputStream()));
                        debuggeeOutputReader.lines().forEach(System.out::println);
                        System.exit(0);
                    }

                    if (event instanceof ClassPrepareEvent) {
                        debugger.handleClassPrepareEvent((ClassPrepareEvent) event);
                    }

                    if (event instanceof StepEvent || event instanceof BreakpointEvent) {
                        handleStepAndBreakpointEvents(scanner, virtualMachine, event);
                    }
                }

                virtualMachine.resume();
            }
        } catch (VMDisconnectedException e) {
            e.printStackTrace();
        }
    }

    private static void handleStepAndBreakpointEvents(final Scanner scanner, final VirtualMachine virtualMachine, final Event event) throws IncompatibleThreadStateException, AbsentInformationException {
        event.request().disable();
        ThreadReference thread = ((LocatableEvent) event).thread();
        StackFrame stackFrame = thread.frame(0);
        final Map<LocalVariable, Value> variables = stackFrame.getValues(stackFrame.visibleVariables());

        System.out.println("Stopped at " + stackFrame.location());

        while (true) {
            System.out.print("$> ");
            final String command = scanner.nextLine().strip();

            if (command.equals("print")) {
                variables.forEach((key, value) -> System.out.println(key.name() + ": " + value));
                continue;
            }

            if (command.startsWith("print ")) {
                final String variableName = command.substring(6);
                Set<Map.Entry<LocalVariable, Value>> variable = variables.entrySet().stream()
                        .filter(entry -> entry.getKey().name().equals(variableName))
                        .collect(Collectors.toSet());

                if (variable.isEmpty()) {
                    System.out.println("No variable found with the name: " + variableName);
                    continue;
                }

                for (final Map.Entry<LocalVariable, Value> entry : variable) {
                    final LocalVariable key = entry.getKey();
                    final Value value = entry.getValue();
                    System.out.println(key.name() + ": " + value);

                    if (value instanceof ArrayReference) {
                        System.out.println("Content: ");
                        System.out.println(((ArrayReference) value).getValues());
                        continue;
                    }

                    if (value instanceof ObjectReference) {
                        ObjectReference objectReference = (ObjectReference) value;
                        Map<Field, Value> fields = objectReference.getValues(objectReference.referenceType().allFields());

                        System.out.println("Fields: ");
                        System.out.println(fields);
                    }
                }

                continue;
            }

            if (command.equals("step over")) {
                virtualMachine.eventRequestManager().createStepRequest(thread, StepRequest.STEP_LINE, StepRequest.STEP_OVER).enable();
                return;
            }

            if (command.equals("step into")) {
                virtualMachine.eventRequestManager().createStepRequest(thread, StepRequest.STEP_LINE, StepRequest.STEP_INTO).enable();
                return;
            }

            if (command.equals("step out")) {
                virtualMachine.eventRequestManager().createStepRequest(thread, StepRequest.STEP_LINE, StepRequest.STEP_OUT).enable();
                return;
            }

            if (command.equals("continue")) {
                return;
            }

            System.out.println("Invalid command");
        }
    }

    private static Set<Integer> getInitialBreakpoints(final Scanner scanner) {
        System.out.print("Enter breakpoints (line numbers separated by spaces): ");
        final String breakpointInput = scanner.nextLine();

        final Set<Integer> breakpoints = Arrays.stream(breakpointInput.split(" "))
                .filter(Predicate.not(String::isBlank))
                .map(Integer::parseInt)
                .filter(i -> i > 0)
                .collect(Collectors.toSet());

        System.out.println("Starting debugging with breakpoints: " + breakpoints);
        return breakpoints;
    }
}

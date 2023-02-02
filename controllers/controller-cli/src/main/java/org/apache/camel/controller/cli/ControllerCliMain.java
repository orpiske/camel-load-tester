package org.apache.camel.controller.cli;

import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(name = "camel-tester", description = "Apache Camel Perf Tester CLI", mixinStandardHelpOptions = true)
public class ControllerCliMain implements Callable<Integer> {
    private static CommandLine commandLine;

    public static void main(String... args) {
        ControllerCliMain main = new ControllerCliMain();
        commandLine = new CommandLine(main)
                .addSubcommand("test", new CommandLine(new Test()));

        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        commandLine.execute("--help");
        return 0;
    }
}

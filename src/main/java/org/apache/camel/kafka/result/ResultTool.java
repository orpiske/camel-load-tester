package org.apache.camel.kafka.result;

import java.util.concurrent.Callable;

import picocli.CommandLine;

@CommandLine.Command(name = "ResultTool", mixinStandardHelpOptions = true, version = "Result tool",
        description = "A result tool for the tester")
public class ResultTool implements Callable<Integer> {
    private static CommandLine commandLine;



    public static void main(String... args) {
        commandLine = new CommandLine(new ResultTool())
                .addSubcommand("join", new JoinCommand());

        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        return commandLine.execute("--help");
    }
}

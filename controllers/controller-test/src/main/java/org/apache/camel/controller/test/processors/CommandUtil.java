package org.apache.camel.controller.test.processors;

import java.io.IOException;
import java.time.Duration;

import org.apache.camel.controller.common.types.TestExecution;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CommandUtil {
    private static final Logger LOG = LoggerFactory.getLogger(CommandUtil.class);

    private CommandUtil() {

    }

    public static int executeCommand(TestExecution testExecution, CommandLine cmdLine) throws IOException, InterruptedException {
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        int timeout = testExecution.getTimeout();
        if (timeout == 0) {
            timeout = 15;
        }

        ExecuteWatchdog watchdog = new ExecuteWatchdog(Duration.ofMinutes(timeout).toMillis());

        Executor executor = new DefaultExecutor();
        executor.setExitValue(1);
        executor.setWatchdog(watchdog);
        executor.execute(cmdLine, resultHandler);

        resultHandler.waitFor();

        final int exitValue = resultHandler.getExitValue();
        LOG.info("Finished with status: {}", exitValue);
        return exitValue;
    }
}

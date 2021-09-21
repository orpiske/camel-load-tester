package org.apache.camel.kafka.result;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.camel.util.IOHelper;
import picocli.CommandLine;

public class JoinCommand implements Callable<Integer> {
    @CommandLine.Option(names = { "--output" }, description = "The output", defaultValue = "output.csv")
    private String output;

    @CommandLine.Option(names = { "--baseline" }, description = "The baseline", arity = "1")
    private String baseline;

    @CommandLine.Option(names = { "--test-titles" }, split = ",", description = "The tests titles to join (in order)", arity = "1")
    private List<String> titles;

    @CommandLine.Option(names = { "--tests" }, split = ",", description = "The tests to join", arity = "1")
    private List<File> tests;

    private void join(BufferedWriter bw, BufferedReader baseLineReader, List<BufferedReader> testReaders) {
        try {
            bw.write("baseline;");

            for (String title : titles) {
                bw.write(title);
                bw.write(";");
            }
            bw.newLine();

            doJoin(bw, baseLineReader, testReaders);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void doJoin(BufferedWriter bw, BufferedReader baseLineReader, List<BufferedReader> testReaders) throws IOException {
        String baseLineLine = baseLineReader.readLine();

        while (baseLineLine != null) {
            bw.write(baseLineLine);
            bw.write(";");

            for (BufferedReader br : testReaders) {
                String testLine = br.readLine();

                if (testLine == null) {
                    testLine = "0";
                }

                bw.write(testLine);
                bw.write(";");
            }

            bw.newLine();
            baseLineLine = baseLineReader.readLine();
        }
    }

    @Override
    public Integer call() throws Exception {

        if (titles.size() != tests.size()) {
            System.out.println("The should be as many titles as tests");
            return 1;
        }

        List<BufferedReader> testReaders = new ArrayList<>(tests.size());
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(output))) {
            try (BufferedReader baseLineReader = new BufferedReader(new FileReader(baseline))) {
                try {
                    for (File test : tests) {
                        testReaders.add(new BufferedReader(new FileReader(test)));
                    }

                    join(bw, baseLineReader, testReaders);
                } finally {
                    testReaders.forEach(r -> IOHelper.close(r));
                }
            }
        }

        return 0;
    }
}

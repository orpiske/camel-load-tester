package org.apache.camel.load.tester.routes;

import org.apache.camel.CamelContext;
import org.apache.camel.Processor;
import org.apache.camel.component.dataset.SimpleDataSet;
import org.apache.camel.main.Main;

public final class DataSetHelper {
    public static final String DATASET_NAME = "testSet";

    private DataSetHelper() {

    }

    public static void bindDataSet(Main main, int testSize) {
        bindDataSet(main, testSize, null);
    }

    public static void bindDataSet(Main main, int testSize, Processor outputTransformer) {
        SimpleDataSet simpleDataSet = new SimpleDataSet();

        simpleDataSet.setDefaultBody(Boolean.TRUE);
        simpleDataSet.setSize(testSize);
        simpleDataSet.setDefaultBody("{\"value\":\"data\"}");

        if (outputTransformer != null) {
            simpleDataSet.setOutputTransformer(outputTransformer);
        }

        main.bind(DATASET_NAME, simpleDataSet);
    }

    public static SimpleDataSet lookupDataSet(CamelContext context) {
        return context.getRegistry().lookupByNameAndType(DataSetHelper.DATASET_NAME, SimpleDataSet.class);
    }

}

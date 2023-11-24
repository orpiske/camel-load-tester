/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.load.analyzer;

import java.util.Locale;

public class ChartProperties {
    private String title = "Rate distribution over time";
    private String seriesName = "Throughput rate";
    private String xTitle = "";
    private String yTitle = "Messages p/ second";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public String getxTitle() {
        if (xTitle != null && xTitle.length() >= 2) {
            return capitilize(xTitle);
        }
        else {
            return xTitle;
        }
    }

    public void setxTitle(String xTitle) {
        this.xTitle = xTitle;
    }

    public String getyTitle() {
        if (yTitle != null && yTitle.length() >= 2) {
            return capitilize(yTitle);
        }
        else {
            return yTitle;
        }
    }

    public static String capitilize(String text) {
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public static String capitilizeOnly(String text) {
        return text.substring(0, 1).toUpperCase() + text.toLowerCase(Locale.ROOT).substring(1);
    }

    public void setyTitle(String yTitle) {
        this.yTitle = yTitle;
    }
}

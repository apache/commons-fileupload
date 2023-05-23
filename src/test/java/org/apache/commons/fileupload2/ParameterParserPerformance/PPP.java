/**<!--Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.--> */

package org.apache.commons.fileupload2.ParameterParserPerformance;

import org.apache.commons.fileupload2.ParameterParser;
import org.openjdk.jmh.annotations.*;
@State(Scope.Benchmark)
public class PPP {
    @Param({ "true", "false" })
    public boolean b;

    ParameterParser pp;

    @Setup(Level.Invocation)
    public void setUp() {
        pp = new ParameterParser();
    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 1)
    @Measurement(iterations = 2)
    @BenchmarkMode(Mode.AverageTime)
    public void testSomeCode(PPP plan)
    {
        pp.setLowerCaseNames(plan.b);
    }
}


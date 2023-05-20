
/*
<?xml version="1.0" encoding="UTF-8"?>
        <!--
        Licensed to the Apache Software Foundation (ASF) under one or more
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
        limitations under the License.
        -->
 * This file was automatically generated by EvoSuite
 * Sat M ay 20 20:34:37 GMT 2023
 */

package org.apache.commons.fileupload2;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.commons.fileupload2.FileItemFactory;
import org.apache.commons.fileupload2.FileUpload;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = true) 
public class FileUpload_ESTest extends FileUpload_ESTest_scaffolding {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      FileUpload fileUpload0 = new FileUpload();
      assertEquals((-1L), fileUpload0.getFileCountMax());
  }

  @Test(timeout = 4000)
  public void test1()  throws Throwable  {
      FileUpload fileUpload0 = new FileUpload((FileItemFactory) null);
      FileItemFactory fileItemFactory0 = fileUpload0.getFileItemFactory();
      assertNull(fileItemFactory0);
  }

  @Test(timeout = 4000)
  public void test2()  throws Throwable  {
      FileUpload fileUpload0 = new FileUpload((FileItemFactory) null);
      fileUpload0.setFileItemFactory((FileItemFactory) null);
      assertNull(fileUpload0.getHeaderEncoding());
  }
}

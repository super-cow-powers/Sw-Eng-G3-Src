/*
 * Copyright (c) 2022, David Miall<dm1306@york.ac.uk>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the copyright holder nor the names of its contributors may
 *   be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package g3.project.xmlIO;

import nu.xom.Document;
import org.junit.jupiter.api.*;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.nio.file.FileSystem;
import java.util.Optional;
import java.util.Scanner; // Import the Scanner class to read text files
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */


public class IoTest {

    static IO test;
    static String presFilePath;
    private static final Integer TEST_ITERATIONS = 1000;

    public IoTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        System.out.println("\n*** Io class tests ***");
    }

        @AfterAll
        public static void tearDownClass () {
            System.out.println("Io class tests complete.");
        }

        @BeforeEach
        public void setUp () {
            System.out.println("New test running...");
            presFilePath = "/Users/pezer/Desktop/3rdYear/SwEng/Sw-Eng-G3-Src-master/src/test/testFile.txt";
            test = new IO(presFilePath) {
                @Override
                protected Optional<Document> retrieveDoc(FileSystem fs) {
                    return Optional.empty();
                }
            };
        }

        @AfterEach
        public void tearDown () {
            System.out.println("Test complete.");
        }

        /**
         * Test of getDoc method, of class Io.
         */
        @Test
        public void testGetDoc () {
            System.out.println("Test: getDoc");
            File file = new File(presFilePath);

            String expectedData = "Test.";
            String actualData = String.valueOf(test.getDoc());

            try {
                Scanner myReader = new Scanner(file);
                expectedData = myReader.nextLine();
                assertSame(expectedData, actualData);
            } catch (FileNotFoundException e) {
                System.out.println("Unable to locate test file.");
            }

        }

        /**
         * Test of canSave method, of class Io.
         */
        @Test
        public void testCanSave () {
            System.out.println("Test: canSave");
            assertFalse(test.canSave());
        }

        /**
         * Test of pathToUriString method, of class Io.
         */
        @Test
        public void testPathToUriString () {
            System.out.println("Test: pathToUriString");
            String path[] = {"/newPath", "http:/path", "http:\\path"};

            for (int i = 0; i < 2; i++) {
                if (i == 0){
                    assertEquals("file://" + path[i], test.pathToUriString(path[i]));
                } else
                if (i == 1){
                    assertEquals(path[i], test.pathToUriString(path[i]));
                } else
                if (i == 2){
                    assertEquals("file://" + path[i], test.pathToUriString(path[i]));
                }
            }
        }

        /**
         * Test of maybeURI method, of class Io.
         */
        @Test
        public void testMaybeURI () throws Exception{
            System.out.println("Test: maybeURI");

            String urString = "invalidString";

            assertEquals(Optional.of("invalidString").toString(), test.maybeURI(urString).toString());
        }

        /**
         * Test of close method, of class Io.
         */
        @Test
        public void testClose () {
            System.out.println("Test: close");
            test.close();
        }

        /**
        * Test of isURiInternal method, of class Io.
        */
        @Test
        public void testIsURIInternal() {
            System.out.println("Test11: isURiInternal");
            assertTrue(test.isUriInternal("internalPath"));
            assertFalse(test.isUriInternal("http://notAnInternalPath"));
            assertFalse(test.isUriInternal("file://notAnInternalPath)"));
        }
    }

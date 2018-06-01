package uk.gov.ons.ctp.response.sample;

import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestFiles {

    /**
     * take a named test file and create a copy of it - is because the ingester
     * will delete the source csv file after ingest
     *
     * @param fileName source file name
     * @return the newly created file
     * @throws Exception oops
     */
    public static MockMultipartFile getTestFile(String fileName) throws Exception {
        Path csvFileLocation = Paths.get(TestFiles.class.getClassLoader().getResource("csv/" + fileName).toURI());
        return new MockMultipartFile("file", fileName, "csv",
                Files.readAllBytes(csvFileLocation));
    }

    public static MockMultipartFile getTestFileFromString(String content) {
        return new MockMultipartFile("file", "fileName", "csv",
                content.getBytes());
    }
}

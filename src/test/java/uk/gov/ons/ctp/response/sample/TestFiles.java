package uk.gov.ons.ctp.response.sample;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.mock.web.MockMultipartFile;

public class TestFiles {

  public static MockMultipartFile getTestFile(String fileName) throws Exception {
    Path csvFileLocation =
        Paths.get(TestFiles.class.getClassLoader().getResource("csv/" + fileName).toURI());
    return new MockMultipartFile("file", fileName, "csv", Files.readAllBytes(csvFileLocation));
  }

  public static MockMultipartFile getTestFileFromString(String content) {
    return new MockMultipartFile("file", "fileName", "csv", content.getBytes());
  }
}

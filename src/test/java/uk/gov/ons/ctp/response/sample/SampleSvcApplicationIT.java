package uk.gov.ons.ctp.response.sample;

import static org.junit.Assert.assertFalse;

import java.util.function.Supplier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@ContextConfiguration
@ActiveProfiles("test")
public class SampleSvcApplicationIT {

  @Autowired private Supplier<Boolean> kubeCronEnabled;

  @Test
  public void kubeConfigDisabledByDefaut() {
    assertFalse(kubeCronEnabled.get());
  }
}

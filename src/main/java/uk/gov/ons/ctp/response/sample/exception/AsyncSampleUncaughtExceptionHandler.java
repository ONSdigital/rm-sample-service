package uk.gov.ons.ctp.response.sample.exception;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import uk.gov.ons.ctp.response.sample.service.PartyService;

public class AsyncSampleUncaughtExceptionHandler implements AsyncUncaughtExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(PartyService.class);

  @Override
  public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
    LOG.error(
        "Uncaught exception running async method", kv("methodName", method.getName()), throwable);
  }
}

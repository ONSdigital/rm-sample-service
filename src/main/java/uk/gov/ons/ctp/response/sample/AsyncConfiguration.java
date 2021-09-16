package uk.gov.ons.ctp.response.sample;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;

@Slf4j
@Configuration
public class AsyncConfiguration implements AsyncConfigurer {

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return new AsyncUncaughtExceptionHandler() {

      @Override
      public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("Error during async call", ex, kv("called method", method.getName()));
      }
    };
  }
}

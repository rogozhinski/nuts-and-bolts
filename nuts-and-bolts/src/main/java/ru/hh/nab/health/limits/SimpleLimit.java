package ru.hh.nab.health.limits;

import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hh.nab.health.monitoring.LoggingContext;

public class SimpleLimit implements Limit {
  private final int max;
  private final AtomicInteger current = new AtomicInteger(0);
  private final LeakDetector detector;
  private final String name;
  private final int loggingThreshold;
  private final static Logger LOGGER = LoggerFactory.getLogger(SimpleLimit.class);

  public SimpleLimit(int max, LeakDetector leakDetector, String name, int loggingThreshold) {
    this.max = max;
    this.detector = leakDetector;
    this.name = name;
    this.loggingThreshold = loggingThreshold;
  }

  @Override
  public LeaseToken acquire() {
    final LoggingContext lc = LoggingContext.fromCurrentContext();
    if (current.incrementAndGet() > max) {
      current.decrementAndGet();
      LOGGER.debug("acquired,limit:{},token:-,max,current:{}", name, current);
      return null;
    }

    final boolean needLogging = current.get() >= loggingThreshold;

    LeaseToken token = new LeaseToken() {
      @Override
      public void release() {
        detector.released(this);
        current.decrementAndGet();
        lc.enter();
        if (needLogging) {
          LOGGER.debug("released,limit:{},token:{},ok,current:{}", objects(name, hashCode(), current));
        }
        lc.leave();
      }
    };
    detector.acquired(token);

    if (needLogging) {
      LOGGER.debug("acquired,limit:{},token:{},ok,current:{}", objects(name, token.hashCode(), current));
    }
    return token;
  }

  @Override
  public int getMax() {
    return max;
  }

  @Override
  public String getName() {
    return name;
  }

  private static Object[] objects(Object... args) {
    return args;
  }
}

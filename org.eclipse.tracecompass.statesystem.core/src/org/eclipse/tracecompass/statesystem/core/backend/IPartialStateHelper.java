package org.eclipse.tracecompass.statesystem.core.backend;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @since 1.0
 */
public interface IPartialStateHelper {

    void getCheckpoints(long t, long checkpointTime);

    void registerCheckpoints();

    void setCountdown(CountDownLatch checkpointsReady);

    void setCheckpoints(Map<Long, Long> checkpoints);

}

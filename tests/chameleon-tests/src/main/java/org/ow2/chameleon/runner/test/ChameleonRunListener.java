package org.ow2.chameleon.runner.test;

import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.ow2.chameleon.runner.test.internals.ChameleonExecutor;

/**
 * a run listener to stop chameleon after tests.
 */
public class ChameleonRunListener extends RunListener {

    @Override
    public void testRunFinished(Result result) throws Exception {
        ChameleonExecutor.stopRunningInstance();
    }
}

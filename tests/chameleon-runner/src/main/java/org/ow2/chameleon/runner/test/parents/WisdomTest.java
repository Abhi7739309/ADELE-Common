package org.ow2.chameleon.runner.test.parents;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.ow2.chameleon.runner.test.ChameleonRunner;
import org.ow2.chameleon.testing.helpers.Stability;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
@RunWith(ChameleonRunner.class)
public class WisdomTest {

    @Inject
    public BundleContext context;


    @Before
    public void ensureBundleContextInjection() throws ClassNotFoundException {
        assertThat(context).isNotNull();
        Stability.waitForStability(context);
    }




}

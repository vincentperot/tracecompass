/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.pcap.core.tests.packet;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Generic Packet test suite
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        PacketTest.class,
        BadPacketTest.class
})
public class AllTests {

}

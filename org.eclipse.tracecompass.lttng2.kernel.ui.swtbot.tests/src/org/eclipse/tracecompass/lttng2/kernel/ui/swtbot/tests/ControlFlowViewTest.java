/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.keyboard.Keyboard;
import org.eclipse.swtbot.swt.finder.keyboard.KeyboardFactory;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.tracecompass.analysis.os.linux.ui.views.controlflow.ControlFlowView;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.junit.Before;
import org.junit.Test;

/**
 * SWTBot tests for Control Flow view
 *
 * @author Patrick Tasse
 */
public class ControlFlowViewTest extends KernelTest {

    private static final String FOLLOW_CPU_BACKWARD = "Follow CPU Backward";
    private static final String FOLLOW_CPU_FORWARD = "Follow CPU Forward";
    private static final String SELECT_PREVIOUS_EVENT = "Select Previous Event";
    private static final String SELECT_NEXT_EVENT = "Select Next Event";
    private static final Keyboard KEYBOARD = KeyboardFactory.getSWTKeyboard();
    private static final @NonNull ITmfTimestamp START_TIME = new TmfNanoTimestamp(1368000272650993664L);
    private static final @NonNull ITmfTimestamp TID1_TIME1 = new TmfNanoTimestamp(1368000272651208412L);
    private static final @NonNull ITmfTimestamp TID1_TIME2 = new TmfNanoTimestamp(1368000272656147616L);
    private static final @NonNull ITmfTimestamp TID1_TIME3 = new TmfNanoTimestamp(1368000272656362364L);
    private static final @NonNull ITmfTimestamp TID1_TIME4 = new TmfNanoTimestamp(1368000272663234300L);
    private static final @NonNull ITmfTimestamp TID1_TIME5 = new TmfNanoTimestamp(1368000272663449048L);
    private static final @NonNull ITmfTimestamp TID1_TIME6 = new TmfNanoTimestamp(1368000272665596528L);
    private static final @NonNull ITmfTimestamp TID2_TIME1 = new TmfNanoTimestamp(1368000272651852656L);
    private static final @NonNull ITmfTimestamp TID2_TIME2 = new TmfNanoTimestamp(1368000272652067404L);
    private static final @NonNull ITmfTimestamp TID2_TIME3 = new TmfNanoTimestamp(1368000272652282152L);
    private static final @NonNull ITmfTimestamp TID2_TIME4 = new TmfNanoTimestamp(1368000272652496900L);
    private static final @NonNull ITmfTimestamp TID5_TIME1 = new TmfNanoTimestamp(1368000272652496900L);

    private SWTBotView fViewBot;

    /**
     * Before Test
     */
    @Override
    @Before
    public void before() {
        super.before();
        fViewBot = fBot.viewByTitle("Control Flow");
    }

    /**
     * Test tool bar buttons "Select Next Event" and "Select Previous Event"
     */
    @Test
    public void testKeyboardLeftRight() {
        final ControlFlowView cfv = (ControlFlowView) fViewBot.getViewReference().getView(false);

        /* change window range to 10 ms */
        TmfTimeRange range = new TmfTimeRange(START_TIME, START_TIME.normalize(10000000L, ITmfTimestamp.NANOSECOND_SCALE));
        cfv.broadcast(new TmfWindowRangeUpdatedSignal(this, range));

        /* set selection to trace start time */
        cfv.broadcast(new TmfSelectionRangeUpdatedSignal(this, START_TIME));

        /* select first item */
        final SWTBotTree tree = fViewBot.bot().tree();
        tree.select(0);

        /* set focus on time graph */
        final TimeGraphControl timegraph = fViewBot.bot().widget(WidgetOfType.widgetOfType(TimeGraphControl.class));
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                timegraph.setFocus();
            }
        });

        /* press ARROW_RIGHT 3 times */
        KEYBOARD.pressShortcut(Keystrokes.RIGHT);
        KEYBOARD.pressShortcut(Keystrokes.RIGHT);
        KEYBOARD.pressShortcut(Keystrokes.RIGHT);
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME3, TID1_TIME3)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME3));

        /* press Shift-ARROW_RIGHT 3 times */
        KEYBOARD.pressShortcut(Keystrokes.SHIFT, Keystrokes.RIGHT);
        KEYBOARD.pressShortcut(Keystrokes.SHIFT, Keystrokes.RIGHT);
        KEYBOARD.pressShortcut(Keystrokes.SHIFT, Keystrokes.RIGHT);
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME3, TID1_TIME6)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME6));

        /* press Shift-ARROW_LEFT 4 times */
        KEYBOARD.pressShortcut(Keystrokes.SHIFT, Keystrokes.LEFT);
        KEYBOARD.pressShortcut(Keystrokes.SHIFT, Keystrokes.LEFT);
        KEYBOARD.pressShortcut(Keystrokes.SHIFT, Keystrokes.LEFT);
        KEYBOARD.pressShortcut(Keystrokes.SHIFT, Keystrokes.LEFT);
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME3, TID1_TIME2)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME2));

        /* press ARROW_RIGHT 2 times */
        KEYBOARD.pressShortcut(Keystrokes.RIGHT);
        KEYBOARD.pressShortcut(Keystrokes.RIGHT);
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME4, TID1_TIME4)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME4));

        /* press Shift-ARROW_LEFT 3 times */
        KEYBOARD.pressShortcut(Keystrokes.SHIFT, Keystrokes.LEFT);
        KEYBOARD.pressShortcut(Keystrokes.SHIFT, Keystrokes.LEFT);
        KEYBOARD.pressShortcut(Keystrokes.SHIFT, Keystrokes.LEFT);
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME4, TID1_TIME1)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME1));

        /* press Shift-ARROW_RIGHT 4 times */
        KEYBOARD.pressShortcut(Keystrokes.SHIFT, Keystrokes.RIGHT);
        KEYBOARD.pressShortcut(Keystrokes.SHIFT, Keystrokes.RIGHT);
        KEYBOARD.pressShortcut(Keystrokes.SHIFT, Keystrokes.RIGHT);
        KEYBOARD.pressShortcut(Keystrokes.SHIFT, Keystrokes.RIGHT);
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME4, TID1_TIME5)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME5));

        /* press ARROW_LEFT 5 times */
        KEYBOARD.pressShortcut(Keystrokes.LEFT);
        KEYBOARD.pressShortcut(Keystrokes.LEFT);
        KEYBOARD.pressShortcut(Keystrokes.LEFT);
        KEYBOARD.pressShortcut(Keystrokes.LEFT);
        KEYBOARD.pressShortcut(Keystrokes.LEFT);
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(START_TIME, START_TIME)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(START_TIME));
    }

    /**
     * Test tool bar buttons "Select Next Event" and "Select Previous Event"
     */
    @Test
    public void testToolBarSelectNextPreviousEvent() {
        final ControlFlowView cfv = (ControlFlowView) fViewBot.getViewReference().getView(false);

        /* change window range to 10 ms */
        TmfTimeRange range = new TmfTimeRange(START_TIME, START_TIME.normalize(10000000L, ITmfTimestamp.NANOSECOND_SCALE));
        cfv.broadcast(new TmfWindowRangeUpdatedSignal(this, range));

        /* set selection to trace start time */
        cfv.broadcast(new TmfSelectionRangeUpdatedSignal(this, START_TIME));

        /* select first item */
        final SWTBotTree tree = fViewBot.bot().tree();
        tree.select(0);

        /* set focus on time graph */
        final TimeGraphControl timegraph = fViewBot.bot().widget(WidgetOfType.widgetOfType(TimeGraphControl.class));
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                timegraph.setFocus();
            }
        });

        /* click "Select Next Event" 3 times */
        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click();
        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click();
        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click();
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME3, TID1_TIME3)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME3));

//        /* shift-click "Select Next Event" 3 times */
//        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click(SWT.SHIFT);
//        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click(SWT.SHIFT);
//        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click(SWT.SHIFT);
//        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME3, TID1_TIME6)));
        /* click "Select Next Event" 3 times */
        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click();
        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click();
        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click();
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME6, TID1_TIME6)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME6));

//        /* shift-click "Select Previous Event" 4 times */
//        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click(SWT.SHIFT);
//        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click(SWT.SHIFT);
//        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click(SWT.SHIFT);
//        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click(SWT.SHIFT);
//        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME3, TID1_TIME2)));
        /* click "Select Previous Event" 4 times */
        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click();
        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click();
        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click();
        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click();
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME2, TID1_TIME2)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME2));

        /* click "Select Next Event" 2 times */
        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click();
        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click();
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME4, TID1_TIME4)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME4));

//        /* shift-click "Select Previous Event" 3 times */
//        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click(SWT.SHIFT);
//        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click(SWT.SHIFT);
//        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click(SWT.SHIFT);
//        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME4, TID1_TIME1)));
        /* click "Select Previous Event" 3 times */
        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click();
        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click();
        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click();
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME1, TID1_TIME1)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME1));

//        /* shift-click "Select Next Event" 4 times */
//        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click(SWT.SHIFT);
//        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click(SWT.SHIFT);
//        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click(SWT.SHIFT);
//        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click(SWT.SHIFT);
//        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME4, TID1_TIME5)));
        /* click "Select Next Event" 4 times */
        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click();
        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click();
        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click();
        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click();
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME5, TID1_TIME5)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME5));

        /* click "Select Previous Event" 5 times */
        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click();
        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click();
        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click();
        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click();
        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click();
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(START_TIME, START_TIME)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(START_TIME));
    }

    /**
     * Test tool bar buttons "Follow CPU Forward" and "Follow CPU Backward"
     */
    @Test
    public void testToolBarFollowCPUForwardBackward() {
        final ControlFlowView cfv = (ControlFlowView) fViewBot.getViewReference().getView(false);

        /* change window range to 10 ms */
        TmfTimeRange range = new TmfTimeRange(START_TIME, START_TIME.normalize(10000000L, ITmfTimestamp.NANOSECOND_SCALE));
        cfv.broadcast(new TmfWindowRangeUpdatedSignal(this, range));

        /* set selection to trace start time */
        cfv.broadcast(new TmfSelectionRangeUpdatedSignal(this, START_TIME));

        /* select first item */
        final SWTBotTree tree = fViewBot.bot().tree();
        tree.select(0);

        /* set focus on time graph */
        final TimeGraphControl timegraph = fViewBot.bot().widget(WidgetOfType.widgetOfType(TimeGraphControl.class));
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                timegraph.setFocus();
            }
        });

        /* click "Follow CPU Forward" 3 times */
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME2, TID2_TIME2)));
        assertEquals("2", tree.selection().get(0, 1));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID2_TIME2));

//        /* shift-click "Follow CPU Forward" 3 times */
//        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
//        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
//        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
//        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME2, TID5_TIME1)));
        /* click "Follow CPU Forward" 3 times */
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID5_TIME1, TID5_TIME1)));
        assertEquals("5", tree.selection().get(0, 1));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID5_TIME1));

//        /* shift-click "Follow CPU Backward" 4 times */
//        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
//        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
//        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
//        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
//        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME2, TID2_TIME1)));
        /* click "Follow CPU Backward" 4 times */
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME1, TID2_TIME1)));
        assertEquals("2", tree.selection().get(0, 1));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID2_TIME1));

        /* click "Follow CPU Forward" 2 times */
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME3, TID2_TIME3)));
        assertEquals("2", tree.selection().get(0, 1));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID2_TIME3));

//        /* shift-click "Follow CPU Backward" 3 times */
//        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
//        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
//        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
//        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME3, TID1_TIME1)));
        /* click "Follow CPU Backward" 3 times */
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME1, TID1_TIME1)));
        assertEquals("1", tree.selection().get(0, 1));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME1));

//        /* shift-click "Follow CPU Forward" 4 times */
//        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
//        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
//        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
//        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
//        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME3, TID2_TIME4)));
        /* click "Follow CPU Forward" 4 times */
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME4, TID2_TIME4)));
        assertEquals("2", tree.selection().get(0, 1));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID2_TIME4));

        /* click "Follow CPU Backward" 5 times */
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(START_TIME, START_TIME)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(START_TIME));
    }
}

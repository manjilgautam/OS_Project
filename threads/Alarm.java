package nachos.threads;
import nachos.machine.*;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.SortedSet;

import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	waiting = new TreeSet<WaitingThread>();
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
	//KThread.currentThread().yield();
	 long time = Machine.timer().getTime();

	if (waiting.isEmpty())
	    return;

	if (((WaitingThread) waiting.first()).time > time)
	    return;

	Lib.debug(dbgInt, "Interrupt at time = " + time);

	while (!waiting.isEmpty() &&
	       ((WaitingThread) waiting.first()).time <= time) {
	    WaitingThread next = (WaitingThread) waiting.first();

        next.thread.ready();
	    waiting.remove(next);

	    Lib.assertTrue(next.time <= time);
	    Lib.debug(dbgInt, "  " + next.thread.getName());
	}

	Lib.debug(dbgInt, "  (Finish timerInterrupt)");
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
	// for now, cheat just to get something working (busy waiting is bad)
	long wakeTime = Machine.timer().getTime() + x;
//	while (wakeTime > Machine.timer().getTime())
//	    KThread.yield();
	boolean intStatus = Machine.interrupt().disable();
	WaitingThread toAlarm = new WaitingThread(wakeTime, KThread.currentThread());

	Lib.debug(dbgInt,
		 "THread Wait ...... " + KThread.currentThread().getName() +
		  " until " + wakeTime);

	waiting.add(toAlarm);

    KThread.sleep();

	Machine.interrupt().restore(intStatus);
 }
}

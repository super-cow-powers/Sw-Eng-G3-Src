/*
 * Copyright (c) 2022, Group 3
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the copyright holder nor the names of its contributors may
 *   be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package g3.project.core;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Group 3
 */
public abstract class Threaded implements Runnable {

    /**
     * Thread I'm to run on.
     */
    private Thread myThread;

    /**
     * Am I running?
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Am I suspended?
     */
    private final AtomicBoolean suspended = new AtomicBoolean(false);

    /**
     * Request start thread activity.
     */
    public final void start() {
        myThread = new Thread(this);
        myThread.start();
        running.set(true);
    }

    /**
     * Request stop thread activity.
     */
    public final void stop() {
        running.set(false);
        unsuspend();
    }

    /**
     * Unsuspend thread.
     */
    protected final synchronized void unsuspend() {
        // Trigger notify if suspended
        if (suspended.get()) {
            suspended.set(false);
            notify();
        }
    }

    /**
     * Getter for myThread
     * @return myThread
     */
    public Thread getThread() {
        return(myThread);
    }

    /**
     * Getter for running
     * @return running
     */
    public AtomicBoolean getRunning() {
        return(running);
    }

    /**
     * Getter for suspended
     * @return suspended
     */
    public AtomicBoolean getSuspended() {
        return(suspended);
    } 

    /**
     * Run stuff.
     */
    @Override
    public abstract void run();

}

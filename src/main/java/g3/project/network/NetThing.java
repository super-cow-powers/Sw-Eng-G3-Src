/*
 * Copyright (c) 2022, David Miall<dm1306@york.ac.uk>
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
package g3.project.network;

import g3.project.core.Threaded;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.event.Event;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public final class NetThing extends Threaded {

    /**
     * Constructor.
     */
    public NetThing() {
        super();
    }

    /**
     * Event queue from input sources.
     */
    private final BlockingQueue<Event> txEventQueue
            = new LinkedBlockingQueue<Event>();

    /**
     * Event queue from input sources.
     */
    private final BlockingQueue<Event> rxEventQueue
            = new LinkedBlockingQueue<Event>();

    /**
     * Send an event to the net.
     *
     * @param event Event to send.
     */
    public void sendEvent(final Event event) {
        txEventQueue.offer(event);
        unsuspend();
    }

    /**
     * Get the RX queue.
     *
     * @return blocking queue for events.
     */
    public BlockingQueue<Event> getRxQueue() {
        return rxEventQueue;
    }

    @Override
    public void run() {
        //Wait till running properly
        while (!(running.get())) {
        }
        //Post-construction Setup goes here
        //...
        while (running.get()) {
            //Main thread dispatch loop
            try {
                if (!txEventQueue.isEmpty()) { // New event to send

                } else {
                    suspended.set(true);
                }

                while (suspended.get()) { // Suspend
                    synchronized (this) {
                        wait();
                    }
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        System.out.println("Net-thing is going down NOW.");
        return;
    }

}

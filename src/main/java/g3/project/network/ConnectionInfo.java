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

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public final class ConnectionInfo {

    /**
     * Host Location.
     */
    private final String hostLoc;
    /**
     * Host ID.
     */
    private final String hostId;
    /**
     * Host Port.
     */
    private final Integer port;

    /**
     * Constructor.
     *
     * @param hostLocString Host location/address.
     * @param hostIdString Host ID.
     * @param hostPort Port to connect to.
     */
    public ConnectionInfo(final String hostLocString, final String hostIdString, final Integer hostPort) {
        this.hostLoc = hostLocString;
        this.hostId = hostIdString;
        this.port = hostPort;
    }

    /**
     * Get host location.
     *
     * @return Host Location.
     */
    public String getHostLoc() {
        return hostLoc;
    }

    /**
     * Get host ID.
     *
     * @return Host ID.
     */
    public String getHostId() {
        return hostId;
    }

    /**
     * Get host Port.
     *
     * @return Host Port.
     */
    public Integer getPort() {
        return port;
    }

}
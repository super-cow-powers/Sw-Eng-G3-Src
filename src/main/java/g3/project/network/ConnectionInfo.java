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
     * Host Port.
     */
    private final Integer port;
    /**
     * Conection Type.
     */
    private final String type;

    /**
     * Constructor.
     *
     * @param hostLocString Host location/address. (e.g. "localhost")
     * @param hostPort Port to connect to. (e.g. "8080")
     * @param connectionType Type of connection. (e.g. "client")
     */
    public ConnectionInfo(final String hostLocString, final Integer hostPort, final String connectionType) {
        this.hostLoc = hostLocString;
        this.port = hostPort;
        this.type = connectionType;
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
     * Get host Port.
     *
     * @return Host Port.
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Get connection type.
     *
     * @return Connection Type.
     */
    public String getType() {
        return type;
    }
}

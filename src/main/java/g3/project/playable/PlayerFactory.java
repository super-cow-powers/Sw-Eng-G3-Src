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
package g3.project.playable;

import java.util.HashMap;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;

/**
 *
 * @author David Miall<dm1306@york.ac.uk>
 */
public final class PlayerFactory {
//CHECKSTYLE:OFF

    /**
     * Created Players.
     */
    HashMap<Integer, Player> playerMap = new HashMap<>();
    /**
     * Player Factory.
     */
    MediaPlayerFactory factory = new MediaPlayerFactory();
//CHECKSTYLE:ON

    /**
     * Constructor.
     */
    public PlayerFactory() {

    }

    /**
     * Make a new player.
     *
     * @return Player.
     */
    public Player newPlayer() {
        return newPlayer(0d, 0d);
    }

    /**
     * Get a new player.
     *
     * @param width Player target width.
     * @param height Player target height.
     * @return player.
     */
    public Player newPlayer(final Double width, final Double height) {
        var player = new Player(width, height, factory);
        playerMap.put(player.hashCode(), player);
        return player;
    }

    /**
     * Close all players and free resources.
     */
    public void freeAll() {
        playerMap.forEach((hashCode, pl) -> free(pl));
        factory.release();
    }

    /**
     * Free a player object's native resources.
     *
     * @param pl player to free.
     * @throws IllegalStateException Couldn't find player in my map.
     */
    public void free(final Player pl) throws IllegalStateException {
        var intPl = playerMap.remove(pl.hashCode());
        if (intPl != null) {
            intPl.free();
        } else {
            pl.free();
            throw new IllegalStateException("Hashcode has changed since player was created.");
        }
    }
}

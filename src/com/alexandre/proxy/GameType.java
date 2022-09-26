package com.alexandre.proxy;

import java.io.File;

public enum GameType {
    AFK(new File("../afk")),
    LOBBY(new File("../lobby")),
    BEDWARS_SOLO(new File("../bedwars_solo"));

    private final File workingDir;
    private ServerPool pool = null;

    GameType(File workingDir) {
        this.workingDir = workingDir;
    }

    public void loadPool() {
        if (this.pool != null) return;
        this.pool = new ServerPool(this.getWorkingDir(), this);
        Main.getServerPools().add(this.getPool());
    }

    public File getWorkingDir() {
        return this.workingDir;
    }

    public ServerPool getPool() {
        return this.pool;
    }
}

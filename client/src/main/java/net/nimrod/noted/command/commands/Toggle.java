package net.nimrod.noted.command.commands;

import net.nimrod.noted.Noted;
import net.nimrod.noted.command.Command;
import net.nimrod.noted.util.LogUtils;

public class Toggle extends Command {

    public Toggle() {
        super("toggle", "Toggles noted-client on or off", "");
    }

    @Override
    public boolean execute(String[] args) {
        if (args.length == 0) {
            Noted.INSTANCE.songPlayer.active = !Noted.INSTANCE.songPlayer.active;

            if (!Noted.INSTANCE.songPlayer.active)
                Noted.INSTANCE.songPlayer.reset();

            LogUtils.chatLog("Toggled noted-client " + (Noted.INSTANCE.songPlayer.active ? "on" : "off"));
            return true;
        }

        return false;
    }

}

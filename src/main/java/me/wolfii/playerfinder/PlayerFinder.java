package me.wolfii.playerfinder;

import me.wolfii.playerfinder.render.Rendermode;

import java.util.HashSet;

public class PlayerFinder {
    public static HashSet<String> highlightedPlayers = new HashSet<>();
    public static boolean hightLightAll = true;
    public static Rendermode lastRendermode = Rendermode.BOTH;
    public static Rendermode rendermode = Rendermode.NONE;
}


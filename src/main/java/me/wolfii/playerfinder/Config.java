package me.wolfii.playerfinder;

public class Config {
    //@Todo make config persist after game restart
    public static boolean hideVanillaHitboxes = true;
    public static boolean showRealNamesWhenHighlighted = false;
    public static boolean renderNametagsThroughWalls = true;
    public static double minimumDistanceSquared = 1.5 * 1.5;
    public static double maximumDistanceSquared = 256*256;
    public static boolean renderEyeHeight = true;
    public static boolean renderFacing = true;
}

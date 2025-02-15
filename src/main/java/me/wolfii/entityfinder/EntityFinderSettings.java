package me.wolfii.entityfinder;

public class EntityFinderSettings {
    //@Todo make config persist after game restart
    public static boolean hideVanillaHitboxes = true;

    public static double minimumDistanceSquared = 1.5 * 1.5;
    public static double maximumDistanceSquared = 256 * 256;

    public static boolean renderEyeHeight = true;
    public static boolean renderFacing = true;
    public static boolean renderTracers = true;

    public static boolean hideSelf = true;
}

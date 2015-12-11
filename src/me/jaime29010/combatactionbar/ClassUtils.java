package me.jaime29010.combatactionbar;

public class ClassUtils {
    public static boolean isPresent(String className) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }
}

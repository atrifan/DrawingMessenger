package ro.idp.dashboard.util;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by alexandru.trifan on 26.04.2016.
 */
public class Util {
    private static List<String> possibleColors = getColorOptions();
    public static List<String> getColorOptions() {
        List<String> colors = new ArrayList<>();
        Field[] declaredFields = Color.class.getDeclaredFields();
        ArrayList<Field> staticFields = new ArrayList<Field>();
        for (Field field : declaredFields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                staticFields.add(field);
            }
        }

        for(Field colorField : staticFields) {
            if(colorField.getName().toUpperCase().equals(colorField.getName())) {
                colors.add(colorField.getName());
            }
        }
        return colors;
    }

    public static Color getColorByString(String color) {
        Class  aClass = Color.class;
        Field field = null;
        try {
            field = aClass.getField(color);
            return (Color)field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int randInt(int min, int max) {
        Random rn = new Random();
        int n = max - min + 1;
        int i = rn.nextInt(n);
        return min + i;
    }

    public static String getRandomColor() {
        return possibleColors.get(Util.randInt(0, possibleColors.size() - 1));
    }

}

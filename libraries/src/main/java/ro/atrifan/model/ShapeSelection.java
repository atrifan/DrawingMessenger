package ro.atrifan.model;

/**
 * Created by Enti on 2/27/2016.
 */
public enum ShapeSelection {
    RECTANGLE("rectangle"),
    CIRCLE("circle"),
    LINE("line"),
    ARROW("arrow");

    private String text;

    ShapeSelection(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public static ShapeSelection fromString(String text) {
        if (text != null) {
            for (ShapeSelection b : ShapeSelection.values()) {
                if (text.equalsIgnoreCase(b.text)) {
                    return b;
                }
            }
        }
        return null;
    }
}

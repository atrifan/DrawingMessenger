package ro.atrifan.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandru.trifan on 26.04.2016.
 */
public class DrawingsModel {
    private List<DrawingModel> drawings = new ArrayList<DrawingModel>();

    public List<DrawingModel> getDrawings() {
        return drawings;
    }

    public void setDrawings(List<DrawingModel> drawings) {
        this.drawings = drawings;
    }

    public void addDrawing(DrawingModel drawingModel) {
        this.drawings.add(drawingModel);
    }
}

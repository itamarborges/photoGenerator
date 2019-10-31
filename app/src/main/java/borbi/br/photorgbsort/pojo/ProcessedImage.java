package borbi.br.photorgbsort.pojo;

import android.graphics.Bitmap;

import borbi.br.photorgbsort.enums.Order;

/**
 * Created by Gabriela on 05/06/2016.
 */
public class ProcessedImage {

    private Bitmap bitmap;
    private Order  order;

    public ProcessedImage(Bitmap bitmap, Order order) {
        this.bitmap = bitmap;
        this.order = order;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}

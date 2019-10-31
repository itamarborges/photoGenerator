package borbi.br.photogenerator.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import borbi.br.photogenerator.MainActivity;
import borbi.br.photogenerator.enums.Order;
import borbi.br.photogenerator.enums.RGB;
import borbi.br.photogenerator.pojo.ProcessedImage;
import borbi.br.photogenerator.utils.LogUtils;

/**
 * Created by Gabriela on 05/06/2016.
 */
public class ReorderTask extends AsyncTask<Object,Void,ProcessedImage> {

    private Context mContext;
    private MainActivity.TaskStatusListener mTaskFinishedListener;

    public ReorderTask(Context context, MainActivity.TaskStatusListener taskFinishedListener) {
        mContext = context;
        this.mTaskFinishedListener = taskFinishedListener;

    }
    @Override
    protected ProcessedImage doInBackground(Object[] params) {

        Log.v(ReorderTask.class.getName(),"Começou a ordenar: "+ new Date().toString());
        try {



        Bitmap originBitmap = (Bitmap) params[0];
        final Order sortOrder = (Order)params[1];

        Log.v(LogUtils.makeLogTag(ReorderTask.class),"ent na task, "+ new Date().toString()+ ", sortOrder = "+sortOrder);

        Integer[] a =(Integer[])params[2];

        Arrays.sort(a, new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {

                if (sortOrder.order0 == RGB.Sum) {
                    return (Color.red(o1) + Color.green(o1) + Color.blue(o1)) - (Color.red(o2) + Color.green(o2) + Color.blue(o2));
                } else {

                    int result = getDifference(sortOrder.order0, o1, o2);
                    if (result != 0) {
                        return result;
                    }

                    result = getDifference(sortOrder.order1, o1, o2);
                    if (result != 0) {
                        return result;
                    }

                    result = getDifference(sortOrder.order2, o1, o2);
                    return result;

                }
            }
        });

        Log.v(LogUtils.makeLogTag(ReorderTask.class),"Terminou o sort "+ new Date().toString());
        Bitmap novo = Bitmap.createBitmap(originBitmap.getWidth(),originBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        int[] pixels = new int[a.length];
        for (int i = 0;i<pixels.length;i++){
            pixels[i]=a[i];
        }

        novo.setPixels(pixels, 0, originBitmap.getWidth(), 0, 0, originBitmap.getWidth(), originBitmap.getHeight());

        return new ProcessedImage(novo,sortOrder);

        } catch (Exception e) {
            Toast.makeText(mContext, "Memória insuficiente para ordernar a imagem", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    private int getDifference(RGB sortOrder, Integer color1, Integer color2){
        int diff = 0;
        switch (sortOrder) {
            case Blue:
                diff = getDifferenceBlue(color1, color2);
                break;
            case Red:
                diff = getDifferenceRed(color1, color2);
                break;
            case Green:
                diff = getDifferenceGreen(color1, color2);
                break;
            case Regular:
                return 0;
        }

        return diff;
    }

    private int getDifferenceRed(Integer color1, Integer color2){
        int o1Ax = (color1 & 0x00ff0000) >> 16;
        int o2Ax = (color2 & 0x00ff0000) >> 16;
        return o2Ax - o1Ax;
    }

    private int getDifferenceGreen(Integer color1, Integer color2){
        int o1Ax = (color1 & 0x0000ff00) >> 8;
        int o2Ax = (color2 & 0x0000ff00) >> 8;
        return o2Ax - o1Ax;
    }

    private int getDifferenceBlue(Integer color1, Integer color2){
        int o1Ax = (color1 & 0x000000ff);
        int o2Ax = (color2 & 0x000000ff);
        return o2Ax - o1Ax;
    }


    @Override
    protected void onPostExecute(ProcessedImage processedImage) {
        //chama nteface na actvty
        super.onPostExecute(processedImage);
        this.mTaskFinishedListener.OnTaskFinished(processedImage);

        Log.v(ReorderTask.class.getName(),"Terminou: "+ new Date().toString());
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}

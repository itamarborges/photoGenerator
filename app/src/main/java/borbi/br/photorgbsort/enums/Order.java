package borbi.br.photorgbsort.enums;

import java.io.Serializable;

public enum Order implements Serializable{

    RedGreenBlue(RGB.Red, RGB.Green, RGB.Blue),
    RedBlueGreen(RGB.Red, RGB.Blue, RGB.Green),
    BlueGreenRed(RGB.Blue, RGB.Green, RGB.Red),
    BlueRedGreen(RGB.Blue, RGB.Red, RGB.Green),
    GreenBlueRed(RGB.Green, RGB.Blue, RGB.Red),
    GreenRedBlue(RGB.Green, RGB.Red, RGB.Blue),
    Regular(RGB.Regular, RGB.Regular, RGB.Regular),
    Sum(RGB.Sum, RGB.Sum, RGB.Sum),
    Random(RGB.Random, RGB.Random, RGB.Random);

    public RGB order0;
    public RGB order1;
    public RGB order2;

    Order(RGB order0, RGB order1, RGB order2) {
        this.order0 = order0;
        this.order1 = order1;
        this.order2 = order2;
    }

    private static final long serialVersionUID = -5659072624209865929L;
}

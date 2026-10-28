package datastructures;

import model.Arac;

// Bagla liste (LinkedList) tabanli Kuyruk (Queue) veri yapisi.
// FIFO: Ilk giren ilk cikar.
public class Kuyruk {
    private Node bas;
    private Node son;
    private int boyut;

    // Bos bir kuyruk olusturur.
    public Kuyruk() {
        this.bas = null;
        this.son = null;
        this.boyut = 0;
    }

    // Kuyruk bos mu kontrol eder.
    public boolean isEmpty() {
        return this.boyut == 0;
    }

    // Kuyruktaki eleman sayisini verir.
    public int size() {
        return this.boyut;
    }

    // Bastaki araci kuyruktan cikarmadan verir.
    public Arac peek() {
        if (isEmpty()) return null;
        return (Arac) this.bas.veri;
    }

    // Araci kuyrugun sonuna ekler.
    public void enqueue(Arac arac) {
        Node yeni = new Node(arac);
        if (isEmpty()) {
            this.bas = yeni;
            this.son = yeni;
        } else {
            this.son.sonraki = yeni;
            this.son = yeni;
        }
        this.boyut++;
    }

    // Bastaki araci kuyruktan cikarip verir.
    public Arac dequeue() {
        if (isEmpty()) return null;
        Arac arac = (Arac) this.bas.veri;
        this.bas = this.bas.sonraki;
        this.boyut--;
        if (isEmpty()) this.son = null;
        return arac;
    }

    // Kuyrugu bozmadan icindeki araclari bas-sona sirayla dizi olarak verir.
    public Arac[] diziOlarakAl() {
        Arac[] araclar = new Arac[this.boyut];
        Node simdiki = this.bas;
        int index = 0;
        while (simdiki != null) {
            araclar[index] = (Arac) simdiki.veri;
            simdiki = simdiki.sonraki;
            index++;
        }
        return araclar;
    }
}

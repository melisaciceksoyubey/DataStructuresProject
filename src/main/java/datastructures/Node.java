package datastructures;

public class Node {
    public Object veri;
    public Node sonraki;

    // Yeni bir dugum olusturur.
    public Node(Object veri) {
        this.veri = veri;
        this.sonraki = null;
    }
}

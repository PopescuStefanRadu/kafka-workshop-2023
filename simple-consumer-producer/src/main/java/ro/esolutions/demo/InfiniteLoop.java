package ro.esolutions.demo;

public class InfiniteLoop {
    public static void main(String[] args) {
        try {
            while (true) {
                System.out.println("Hello");
            }
        } finally {
            System.out.println("Bye");
        }
    }
}

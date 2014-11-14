package sample;

public class Starter {
        public static void main(String[] args) {
        NameRunnable nr = new NameRunnable();
        Thread one = new Thread(nr);
        one.setName("Первый");

        Thread two = new Thread(nr);
        two.setName("Второй");

        Thread three = new Thread(nr);
        three.setName("Третий");

        one.start();
        two.start();
        three.start();
        }
        }

class NameRunnable implements Runnable {
    public void run() {
        for (int x = 1; x <= 4; x++) {
            System.out.println("Запущен "
                    + Thread.currentThread().getName()
                    + ", x равен " + x);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }
}
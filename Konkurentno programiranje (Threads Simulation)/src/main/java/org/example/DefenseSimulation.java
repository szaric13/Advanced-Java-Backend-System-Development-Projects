package org.example;

import java.util.*;
import java.util.concurrent.*;
import java.util.Scanner;

public class DefenseSimulation {
    private static final int MAX_VREME_ODBRANE = 5000;
    private static final Random nasumicniBroj = new Random();
    private static volatile long vremePocetka;
    private static List<Integer> ocene = Collections.synchronizedList(new ArrayList<>());
    private static Set<Integer> procesuiraniStudenti = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        Scanner skener = new Scanner(System.in);
        System.out.print("Unesite broj studenata: ");
        int brojStudenata = skener.nextInt();

        vremePocetka = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(10);
        CyclicBarrier barijeraProfesor = new CyclicBarrier(2);
        Semaphore semaforAsistent = new Semaphore(1);
        CountDownLatch krajLatch = new CountDownLatch(brojStudenata);

        for (int i = 0; i < brojStudenata; i++) {
            executor.submit(new ZadacaStudenta(i + 1, barijeraProfesor, semaforAsistent, krajLatch));
        }

        try {
            krajLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor.shutdown();

        if (!ocene.isEmpty()) {
            double prosek = ocene.stream().mapToInt(Integer::intValue).average().orElse(0.0);
            System.out.println("Prosečna ocena: " + prosek);
        } else {
            System.out.println("Nema obrađenih studenata, prosečna ocena ne može da se izračuna.");
        }
    }

    static class ZadacaStudenta implements Runnable {
        private final int idStudenta;
        private final CyclicBarrier barijeraProfesor;
        private final Semaphore semaforAsistent;
        private final CountDownLatch krajLatch;

        public ZadacaStudenta(int idStudenta, CyclicBarrier barijeraProfesor, Semaphore semaforAsistent, CountDownLatch krajLatch) {
            this.idStudenta = idStudenta;
            this.barijeraProfesor = barijeraProfesor;
            this.semaforAsistent = semaforAsistent;
            this.krajLatch = krajLatch;
        }

        @Override
        public void run() {
            try {
                long vremeDolaska = nasumicniBroj.nextInt(1000);
                Thread.sleep(vremeDolaska);

                if (System.currentTimeMillis() - vremePocetka > MAX_VREME_ODBRANE) {
                    System.out.println("Student " + idStudenta + " je zakasnio za odbranu.");
                    krajLatch.countDown();
                    return;
                }

                long vremeOdbrane = 500 + nasumicniBroj.nextInt(500);
                Thread.sleep(vremeOdbrane);

                String ispitujući = (nasumicniBroj.nextBoolean()) ? "Profesor" : "Asistent";
                String imeTreda = Thread.currentThread().getName();
                int ocena = 5 + nasumicniBroj.nextInt(6);

                synchronized (procesuiraniStudenti) {
                    if (procesuiraniStudenti.contains(idStudenta)) {
                        return;
                    }
                    procesuiraniStudenti.add(idStudenta);
                }

                if (ispitujući.equals("Profesor")) {
                    barijeraProfesor.await();
                    System.out.println("Tred: " + imeTreda + " Dolazak: " + vremeDolaska + " Prof: " + ispitujući + " Vreme: " + vremeOdbrane + " Početno vreme: " + vremeDolaska + " Ocena: " + ocena);
                } else {
                    semaforAsistent.acquire();
                    System.out.println("Tred: " + imeTreda + " Dolazak: " + vremeDolaska + " Prof: " + ispitujući + " Vreme: " + vremeOdbrane + " Početno vreme: " + vremeDolaska + " Ocena: " + ocena);
                    semaforAsistent.release();
                }

                if (System.currentTimeMillis() - vremePocetka <= MAX_VREME_ODBRANE) {
                    synchronized (ocene) {
                        ocene.add(ocena);
                    }
                }

                krajLatch.countDown();

            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }
}
